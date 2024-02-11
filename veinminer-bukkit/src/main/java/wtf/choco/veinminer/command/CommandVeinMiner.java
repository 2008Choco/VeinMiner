package wtf.choco.veinminer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.data.LegacyImportTask;
import wtf.choco.veinminer.data.PersistentDataStorageSQL;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.update.UpdateResult;
import wtf.choco.veinminer.util.EnumUtil;
import wtf.choco.veinminer.util.StringUtils;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

public final class CommandVeinMiner implements TabExecutor {

    private static final long IMPORT_CONFIRMATION_TIME_MILLIS = TimeUnit.SECONDS.toMillis(20);

    private final Map<CommandSender, Long> requiresConfirmation = new HashMap<>();

    private final VeinMinerPlugin plugin;
    private final TabExecutor commandBlocklist;
    private final TabExecutor commandToollist;

    public CommandVeinMiner(@NotNull VeinMinerPlugin plugin, @NotNull TabExecutor commandBlocklist, @NotNull TabExecutor commandToollist) {
        this.plugin = plugin;
        this.commandBlocklist = commandBlocklist;
        this.commandToollist = commandToollist;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_RELOAD)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            this.plugin.reloadConfig();
            this.plugin.getVeinMinerManager().reloadFromConfig();
            this.plugin.getToolCategoryRegistry().reloadFromConfig();

            // Update configurations for all players
            this.plugin.getPlayerManager().getAll().forEach(veinMinerPlayer -> {
                veinMinerPlayer.setClientConfig(plugin.getConfiguration().getClientConfiguration(veinMinerPlayer.getPlayer()));
            });

            sender.sendMessage(ChatColor.GREEN + "VeinMiner configuration successfully reloaded.");
            return true;
        }

        else if (args[0].equalsIgnoreCase("version")) {
            PluginDescriptionFile description = plugin.getDescription();
            String headerFooter = ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 44);

            sender.sendMessage(headerFooter);
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.WHITE + description.getVersion() + getUpdateSuffix());
            sender.sendMessage(ChatColor.GOLD + "Developer: " + ChatColor.WHITE + description.getAuthors().get(0));
            sender.sendMessage(ChatColor.GOLD + "Plugin page: " + ChatColor.WHITE + description.getWebsite());
            sender.sendMessage(ChatColor.GOLD + "Source code: " + ChatColor.WHITE + "https://github.com/2008Choco/VeinMiner");
            sender.sendMessage("");
            sender.sendMessage(headerFooter);
            return true;
        }

        else if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Vein miner cannot be toggled from the console.");
                return true;
            }

            if (!canVeinMine(player) || !player.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE)) {
                player.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            // Toggle a specific tool
            if (args.length >= 2) {
                VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
                if (category == null) {
                    player.sendMessage(ChatColor.RED + "Invalid tool category " + args[1] + ".");
                    return true;
                }

                veinMinerPlayer.setVeinMinerEnabled(category, !veinMinerPlayer.isVeinMinerEnabled(category));
                player.sendMessage(ChatColor.GRAY + "Vein miner toggled "
                    + (veinMinerPlayer.isVeinMinerEnabled(category)
                            ? ChatColor.GREEN.toString() + ChatColor.BOLD + "ON"
                            : ChatColor.RED.toString() + ChatColor.BOLD + "OFF"
                    )
                    + ChatColor.GRAY + " for tool " + ChatColor.YELLOW + category.getId().toLowerCase() + ChatColor.GRAY + ".");
            }

            // Toggle all tools
            else {
                veinMinerPlayer.setVeinMinerEnabled(!veinMinerPlayer.isVeinMinerEnabled());
                player.sendMessage(ChatColor.GRAY + "Vein miner toggled "
                    + (veinMinerPlayer.isVeinMinerDisabled()
                            ? ChatColor.RED.toString() + ChatColor.BOLD + "OFF"
                            : ChatColor.GREEN.toString() + ChatColor.BOLD + "ON"
                    )
                    + ChatColor.GRAY + " for " + ChatColor.YELLOW + "all tools" + ChatColor.GRAY + ".");
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("mode")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Vein miner modes cannot be changed from the console.");
                return true;
            }

            if (!canVeinMine(player) || !player.hasPermission(VMConstants.PERMISSION_COMMAND_MODE)) {
                player.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("/" + label + " mode <" + Stream.of(ActivationStrategy.values()).map(strategy -> strategy.name().toLowerCase()).collect(Collectors.joining("|")) + ">");
                return true;
            }

            Optional<ActivationStrategy> strategyOptional = EnumUtil.get(ActivationStrategy.class, args[1].toUpperCase());
            if (!strategyOptional.isPresent()) {
                player.sendMessage(ChatColor.RED + "Invalid mode " + args[1] + ".");
                return true;
            }

            ActivationStrategy strategy = strategyOptional.get();
            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            if (strategy == ActivationStrategy.CLIENT && !veinMinerPlayer.isUsingClientMod()) {
                player.sendMessage(ChatColor.RED + "You do not have the VeinMiner Companion mod installed on your client!");

                // Let them know where to install VeinMiner on the client (if it's allowed)
                if (veinMinerPlayer.getClientConfig().isAllowActivationKeybind()) {
                    player.sendMessage("In order to use client activation, you must install a client-sided mod.");
                    player.sendMessage("https://www.curseforge.com/minecraft/mc-mods/veinminer-companion");
                    player.sendMessage("Supports " + ChatColor.GRAY + "Fabric" + ChatColor.RESET + " (support for " + ChatColor.GRAY + "Forge" + ChatColor.RESET + " Soon™)");
                }

                return true;
            }

            veinMinerPlayer.setActivationStrategy(strategy);
            player.sendMessage(ChatColor.GREEN + "Mode successfully changed to " + ChatColor.YELLOW + strategy.name().toLowerCase().replace("_", " ") + ChatColor.GREEN + ".");
            return true;
        }

        else if (args[0].equalsIgnoreCase("blocklist") && sender.hasPermission(VMConstants.PERMISSION_COMMAND_BLOCKLIST)) {
            this.commandBlocklist.onCommand(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("toollist") && sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOOLLIST)) {
            this.commandToollist.onCommand(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("pattern")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Vein miner patterns cannot be changed from the console.");
                return true;
            }

            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("/" + label + " pattern <pattern>");
                return true;
            }

            NamespacedKey patternKey = NamespacedKey.fromString(args[1], "veinminer");
            VeinMiningPattern pattern = plugin.getPatternRegistry().get(patternKey);
            if (pattern == null) {
                sender.sendMessage(ChatColor.RED + "A pattern with the key " + patternKey + " could not be found.");
                return true;
            }

            String permission = pattern.getPermission();
            if (permission != null && !player.hasPermission(permission)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this pattern.");
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            PlayerVeinMiningPatternChangeEvent event = VMEventFactory.callPlayerVeinMiningPatternChangeEvent(player, veinMinerPlayer.getVeinMiningPattern(), pattern, PlayerVeinMiningPatternChangeEvent.Cause.COMMAND);
            if (event.isCancelled()) {
                return true;
            }

            pattern = event.getNewPattern();
            veinMinerPlayer.setVeinMiningPattern(pattern);

            sender.sendMessage(ChatColor.GREEN + "Pattern set to " + pattern.getKey() + ".");
            return true;
        }

        else if (args[0].equalsIgnoreCase("import")) {
            if (!(plugin.getPersistentDataStorage() instanceof PersistentDataStorageSQL dataStorage)) {
                sender.sendMessage(ChatColor.RED + "You are not using MySQL or SQLite storage. You do not need to import data.");
                return true;
            }

            if (System.currentTimeMillis() - requiresConfirmation.getOrDefault(sender, 0L) > IMPORT_CONFIRMATION_TIME_MILLIS) {
                sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "WARNING!");
                sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.ITALIC + "This is a destructive operation");
                sender.sendMessage("");
                sender.sendMessage("""
                        The import command is meant to import data from JSON storage from before the 2.0.0 update.
                        This includes only the player's preferred activation strategy and their disabled categories.
                        If a JSON file represents the data of a player already in the new VeinMiner database, it will overwrite what is in the database.
                        Depending on the amount of unique players on your server, this process may take time.

                        You only need to do this import once.
                        You have 20 seconds to run "/veinminer import" to confirm.
                        """);

                this.requiresConfirmation.put(sender, System.currentTimeMillis());
                return true;
            }

            this.requiresConfirmation.remove(sender);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new LegacyImportTask(plugin, sender, dataStorage));
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            suggestions.add("version");
            this.addConditionally(suggestions, "reload", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_RELOAD));
            this.addConditionally(suggestions, "blocklist", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_BLOCKLIST));
            this.addConditionally(suggestions, "toollist", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOOLLIST));
            this.addConditionally(suggestions, "toggle", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE));
            this.addConditionally(suggestions, "mode", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_MODE));
            this.addConditionally(suggestions, "pattern", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN));
            this.addConditionally(suggestions, "import", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_IMPORT));

            return StringUtils.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        if (args[0].equalsIgnoreCase("blocklist")) {
            return commandBlocklist.onTabComplete(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            return commandToollist.onTabComplete(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();

            if (args[0].equalsIgnoreCase("toggle")) {
                this.plugin.getToolCategoryRegistry().getAll().forEach(category -> suggestions.add(category.getId().toLowerCase()));
            }

            else if (args[0].equalsIgnoreCase("mode") && sender instanceof Player player) {
                VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
                if (veinMinerPlayer == null) {
                    return Collections.emptyList();
                }

                for (ActivationStrategy activationStrategy : ActivationStrategy.values()) {
                    if (activationStrategy == ActivationStrategy.CLIENT && !veinMinerPlayer.getClientConfig().isAllowActivationKeybind()) {
                        continue;
                    }

                    suggestions.add(activationStrategy.name().toLowerCase());
                }
            }

            else if (args[0].equalsIgnoreCase("pattern")) {
                for (VeinMiningPattern pattern : plugin.getPatternRegistry().getPatterns()) {
                    String permission = pattern.getPermission();
                    if (permission != null && !sender.hasPermission(permission)) {
                        continue;
                    }

                    String patternKey = pattern.getKey().toString();
                    if (patternKey.contains(args[1])) {
                        suggestions.add(patternKey);
                    }
                }
            }

            return StringUtils.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    private <T> void addConditionally(Collection<T> collection, T value, BooleanSupplier predicate) {
        if (predicate.getAsBoolean()) {
            collection.add(value);
        }
    }

    private boolean canVeinMine(Player player) {
        for (VeinMinerToolCategory category : plugin.getToolCategoryRegistry().getAll()) {
            if (player.hasPermission(VMConstants.PERMISSION_VEINMINE.apply(category))) {
                return true;
            }
        }

        return false;
    }

    private String getUpdateSuffix() {
        return plugin.getUpdateChecker().getLastUpdateResult()
                .filter(UpdateResult::isUpdateAvailable)
                .map(result -> " (" + ChatColor.GREEN + ChatColor.BOLD + "UPDATE AVAILABLE!" + ChatColor.GRAY + ")")
                .orElseGet(() -> "");
    }

}
