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

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.api.event.player.PatternChangeEvent;
import wtf.choco.veinminer.data.LegacyImportTask;
import wtf.choco.veinminer.data.PersistentDataStorageSQL;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.platform.PlatformCommandSender;
import wtf.choco.veinminer.platform.PlatformPlayer;
import wtf.choco.veinminer.platform.ServerPlatform.VeinMinerDetails;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.update.UpdateResult;
import wtf.choco.veinminer.util.ChatFormat;
import wtf.choco.veinminer.util.EnumUtil;
import wtf.choco.veinminer.util.NamespacedKey;
import wtf.choco.veinminer.util.StringUtils;
import wtf.choco.veinminer.util.VeinMinerConstants;

public final class CommandVeinMiner implements CommandExecutor {

    private static final long IMPORT_CONFIRMATION_TIME_MILLIS = TimeUnit.SECONDS.toMillis(20);

    private final Map<PlatformCommandSender, Long> requiresConfirmation = new HashMap<>();

    private final VeinMinerServer veinMiner;
    private final CommandExecutor commandBlocklist;
    private final CommandExecutor commandToollist;

    public CommandVeinMiner(@NotNull VeinMinerServer veinMiner, @NotNull CommandExecutor commandBlocklist, @NotNull CommandExecutor commandToollist) {
        this.veinMiner = veinMiner;
        this.commandBlocklist = commandBlocklist;
        this.commandToollist = commandToollist;
    }

    @Override
    public boolean execute(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_RELOAD)) {
                sender.sendMessage(ChatFormat.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            VeinMinerServer veinMiner = VeinMinerServer.getInstance();
            veinMiner.getPlatform().getConfig().reload();

            veinMiner.reloadVeinMinerManagerConfig();
            veinMiner.reloadToolCategoryRegistryConfig();

            // Update configurations for all players
            this.veinMiner.getPlayerManager().getAll().forEach(veinMinerPlayer -> {
                veinMinerPlayer.setClientConfig(veinMiner.createClientConfig(veinMinerPlayer.getPlayer()));
            });

            sender.sendMessage(ChatFormat.GREEN + "VeinMiner configuration successfully reloaded.");
            return true;
        }

        else if (args[0].equalsIgnoreCase("version")) {
            VeinMinerDetails details = veinMiner.getPlatform().getVeinMinerDetails();
            String headerFooter = ChatFormat.GOLD.toString() + ChatFormat.BOLD + ChatFormat.STRIKETHROUGH + StringUtils.repeat('-', 44);

            sender.sendMessage(headerFooter);
            sender.sendMessage("");
            sender.sendMessage(ChatFormat.GOLD + "Version: " + ChatFormat.WHITE + details.version() + getUpdateSuffix());
            sender.sendMessage(ChatFormat.GOLD + "Developer: " + ChatFormat.WHITE + details.author());
            sender.sendMessage(ChatFormat.GOLD + "Plugin page: " + ChatFormat.WHITE + details.website());
            sender.sendMessage(ChatFormat.GOLD + "Source code: " + ChatFormat.WHITE + "https://github.com/2008Choco/VeinMiner");
            sender.sendMessage("");
            sender.sendMessage(headerFooter);
            return true;
        }

        else if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof PlatformPlayer player)) {
                sender.sendMessage("Vein miner cannot be toggled from the console.");
                return true;
            }

            if (!canVeinMine(player) || !player.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_TOGGLE)) {
                player.sendMessage(ChatFormat.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = veinMiner.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            // Toggle a specific tool
            if (args.length >= 2) {
                VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(args[1]);
                if (category == null) {
                    player.sendMessage(ChatFormat.RED + "Invalid tool category " + args[1] + ".");
                    return true;
                }

                veinMinerPlayer.setVeinMinerEnabled(category, !veinMinerPlayer.isVeinMinerEnabled(category));
                player.sendMessage(ChatFormat.GRAY + "Vein miner toggled "
                    + (veinMinerPlayer.isVeinMinerEnabled(category)
                            ? ChatFormat.GREEN.toString() + ChatFormat.BOLD + "ON"
                            : ChatFormat.RED.toString() + ChatFormat.BOLD + "OFF"
                    )
                    + ChatFormat.GRAY + " for tool " + ChatFormat.YELLOW + category.getId().toLowerCase() + ChatFormat.GRAY + ".");
            }

            // Toggle all tools
            else {
                veinMinerPlayer.setVeinMinerEnabled(!veinMinerPlayer.isVeinMinerEnabled());
                player.sendMessage(ChatFormat.GRAY + "Vein miner toggled "
                    + (veinMinerPlayer.isVeinMinerDisabled()
                            ? ChatFormat.RED.toString() + ChatFormat.BOLD + "OFF"
                            : ChatFormat.GREEN.toString() + ChatFormat.BOLD + "ON"
                    )
                    + ChatFormat.GRAY + " for " + ChatFormat.YELLOW + "all tools" + ChatFormat.GRAY + ".");
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("mode")) {
            if (!(sender instanceof PlatformPlayer player)) {
                sender.sendMessage("Vein miner modes cannot be changed from the console.");
                return true;
            }

            if (!canVeinMine(player) || !player.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_MODE)) {
                player.sendMessage(ChatFormat.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("/" + label + " mode <" + Stream.of(ActivationStrategy.values()).map(strategy -> strategy.name().toLowerCase()).collect(Collectors.joining("|")) + ">");
                return true;
            }

            Optional<ActivationStrategy> strategyOptional = EnumUtil.get(ActivationStrategy.class, args[1].toUpperCase());
            if (!strategyOptional.isPresent()) {
                player.sendMessage(ChatFormat.RED + "Invalid mode " + args[1] + ".");
                return true;
            }

            ActivationStrategy strategy = strategyOptional.get();
            VeinMinerPlayer veinMinerPlayer = veinMiner.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            if (strategy == ActivationStrategy.CLIENT && !veinMinerPlayer.isUsingClientMod()) {
                player.sendMessage(ChatFormat.RED + "You do not have the VeinMiner Companion mod installed on your client!");

                // Let them know where to install VeinMiner on the client (if it's allowed)
                if (veinMinerPlayer.getClientConfig().isAllowActivationKeybind()) {
                    player.sendMessage("In order to use client activation, you must install a client-sided mod.");
                    player.sendMessage("https://www.curseforge.com/minecraft/mc-mods/veinminer-companion");
                    player.sendMessage("Supports " + ChatFormat.GRAY + "Fabric" + ChatFormat.RESET + " (support for " + ChatFormat.GRAY + "Forge" + ChatFormat.RESET + " Soon™)");
                }

                return true;
            }

            veinMinerPlayer.setActivationStrategy(strategy);
            player.sendMessage(ChatFormat.GREEN + "Mode successfully changed to " + ChatFormat.YELLOW + strategy.name().toLowerCase().replace("_", " ") + ChatFormat.GREEN + ".");
            return true;
        }

        else if (args[0].equalsIgnoreCase("blocklist")) {
            this.commandBlocklist.execute(sender, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            this.commandToollist.execute(sender, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("pattern")) {
            if (!(sender instanceof PlatformPlayer player)) {
                sender.sendMessage("Vein miner patterns cannot be changed from the console.");
                return true;
            }

            if (!sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_PATTERN)) {
                sender.sendMessage(ChatFormat.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("/" + label + " pattern <pattern>");
                return true;
            }

            NamespacedKey patternKey = NamespacedKey.fromString(args[1], "veinminer");
            VeinMiningPattern pattern = veinMiner.getPatternRegistry().get(patternKey);
            if (pattern == null) {
                sender.sendMessage(ChatFormat.RED + "A pattern with the key " + patternKey + " could not be found.");
                return true;
            }

            String permission = pattern.getPermission();
            if (permission != null && !player.hasPermission(permission)) {
                sender.sendMessage(ChatFormat.RED + "You do not have permission to use this pattern.");
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = veinMiner.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            PatternChangeEvent event = veinMiner.getPlatform().getEventDispatcher().callPatternChangeEvent(player, veinMinerPlayer.getVeinMiningPattern(), pattern, PatternChangeEvent.Cause.COMMAND);
            if (event.isCancelled()) {
                return true;
            }

            pattern = event.getNewPattern();
            veinMinerPlayer.setVeinMiningPattern(pattern);

            sender.sendMessage(ChatFormat.GREEN + "Pattern set to " + pattern.getKey() + ".");
            return true;
        }

        else if (args[0].equalsIgnoreCase("import")) {
            if (!(veinMiner.getPersistentDataStorage() instanceof PersistentDataStorageSQL dataStorage)) {
                sender.sendMessage(ChatFormat.RED + "You are not using MySQL or SQLite storage. You do not need to import data.");
                return true;
            }

            if (System.currentTimeMillis() - requiresConfirmation.getOrDefault(sender, 0L) > IMPORT_CONFIRMATION_TIME_MILLIS) {
                sender.sendMessage(ChatFormat.RED.toString() + ChatFormat.BOLD + "WARNING!");
                sender.sendMessage(ChatFormat.DARK_RED.toString() + ChatFormat.ITALIC + "This is a destructive operation");
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
            this.veinMiner.getPlatform().runTaskAsynchronously(new LegacyImportTask(veinMiner, sender, dataStorage));
            return true;
        }

        return false;
    }

    @Override
    public List<String> tabComplete(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            suggestions.add("version");
            this.addConditionally(suggestions, "reload", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_RELOAD));
            this.addConditionally(suggestions, "blocklist", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_BLOCKLIST));
            this.addConditionally(suggestions, "toollist", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_TOOLLIST));
            this.addConditionally(suggestions, "toggle", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_TOGGLE));
            this.addConditionally(suggestions, "mode", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_MODE));
            this.addConditionally(suggestions, "pattern", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_PATTERN));
            this.addConditionally(suggestions, "import", () -> sender.hasPermission(VeinMinerConstants.PERMISSION_COMMAND_IMPORT));

            return StringUtils.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        if (args[0].equalsIgnoreCase("blocklist")) {
            return commandBlocklist.tabComplete(sender, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            return commandToollist.tabComplete(sender, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();

            if (args[0].equalsIgnoreCase("toggle")) {
                this.veinMiner.getToolCategoryRegistry().getAll().forEach(category -> suggestions.add(category.getId().toLowerCase()));
            }

            else if (args[0].equalsIgnoreCase("mode") && sender instanceof PlatformPlayer player) {
                VeinMinerPlayer veinMinerPlayer = veinMiner.getPlayerManager().get(player);
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
                for (VeinMiningPattern pattern : veinMiner.getPatternRegistry().getPatterns()) {
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

    private boolean canVeinMine(PlatformPlayer player) {
        for (VeinMinerToolCategory category : veinMiner.getToolCategoryRegistry().getAll()) {
            if (player.hasPermission(VeinMinerConstants.PERMISSION_VEINMINE.apply(category))) {
                return true;
            }
        }

        return false;
    }

    private String getUpdateSuffix() {
        return veinMiner.getPlatform().getUpdateChecker().getLastUpdateResult()
                .filter(UpdateResult::isUpdateAvailable)
                .map(result -> " (" + ChatFormat.GREEN + ChatFormat.BOLD + "UPDATE AVAILABLE!" + ChatFormat.GRAY + ")")
                .orElseGet(() -> "");
    }

}
