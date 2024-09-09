package wtf.choco.veinminer.command;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.data.LegacyImportTask;
import wtf.choco.veinminer.data.LegacyImportable;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.update.UpdateResult;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

public final class CommandVeinMiner implements TabExecutor {

    private static final long IMPORT_CONFIRMATION_TIME_MILLIS = TimeUnit.SECONDS.toMillis(20);
    private static final List<String> NUMBERS = IntStream.range(1, 10).mapToObj(String::valueOf).toList();
    private static final List<String> SUGGESTION_OPTIONAL_AMOUNT = List.of("[amount]");

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
            this.plugin.getCategoriesConfig().reload();
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
            String headerFooter = ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "-".repeat(44);

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

            Optional<ActivationStrategy> strategyOptional = Enums.getIfPresent(ActivationStrategy.class, args[1].toUpperCase());
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

            NamespacedKey patternKey = NamespacedKey.fromString(args[1], plugin);
            if (patternKey == null) {
                sender.sendMessage(ChatColor.RED + "Invalid key: \"" + args[1] + "\"");
                return true;
            }

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
            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_IMPORT)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (!(plugin.getPersistentDataStorage() instanceof LegacyImportable importable)) {
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new LegacyImportTask(plugin, sender, importable, plugin.getPersistentDataStorage().getType().getName()));
            return true;
        }

        else if (args[0].equalsIgnoreCase("givetool")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can be given category tools.");
                return true;
            }

            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_GIVETOOL)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("/" + label + " givetool <category> <item> [amount]");
                return true;
            }

            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
            if (category == null) {
                sender.sendMessage(ChatColor.RED + "Unknown tool category, " + args[1]);
                return true;
            }

            if (category.getItems().isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Cannot give items from category " + args[1] + " because it does not have any items.");
                return true;
            }

            Material material = Material.matchMaterial(args[2]);
            if (material == null || !material.isItem()) {
                sender.sendMessage(ChatColor.RED + "Unknown item type. " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + args[2] + ChatColor.GRAY + ".");
                return true;
            } else if (!category.getItems().contains(material)) {
                sender.sendMessage(ChatColor.RED + "Unsupported item type, does not belong to " + ChatColor.YELLOW + category.getId() + ChatColor.RED + ". " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + args[2] + ChatColor.GRAY + ".");
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                amount = Math.max(1, parseInt(args[3], 1));
            }

            ItemStack itemStack = category.createItemStack(material, amount);
            if (!player.getInventory().addItem(itemStack).isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Your inventory was too full and the tool could not be given to you!");
            } else {
                sender.sendMessage(ChatColor.GREEN + "Successfully given the tool from category " + category.getId() + " and type " + itemStack.getType().getKey() + ".");
            }

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
            this.addConditionally(suggestions, "givetool", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_GIVETOOL));
            this.addConditionally(suggestions, "toggle", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE));
            this.addConditionally(suggestions, "mode", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_MODE));
            this.addConditionally(suggestions, "pattern", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN));
            this.addConditionally(suggestions, "import", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_IMPORT));

            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
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

            else if (args[0].equalsIgnoreCase("givetool")) {
                this.plugin.getToolCategoryRegistry().getAll().forEach(category -> {
                    if (!category.getItems().isEmpty()) {
                        suggestions.add(category.getId().toLowerCase());
                    }
                });
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

            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }

        else if (args[0].equalsIgnoreCase("givetool")) {
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
            if (category == null) {
                return Collections.emptyList();
            }

            if (args.length == 3) {
                List<String> suggestions = new ArrayList<>();
                category.getItems().forEach(material -> suggestions.add(material.getKey().toString()));
                return StringUtil.copyPartialMatches(args[2], suggestions, new ArrayList<>());
            }

            else if (args.length == 4) {
                if (args[3].isEmpty()) {
                    return SUGGESTION_OPTIONAL_AMOUNT;
                }

                if (isNumber(args[3])) {
                    return Lists.transform(NUMBERS, value -> args[3] + value);
                } else {
                    return NUMBERS;
                }
            }
        }

        return Collections.emptyList();
    }

    private boolean isNumber(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private int parseInt(String input, int defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
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
        UpdateResult result = plugin.getUpdateChecker().getLastUpdateResult().orElse(null);
        if (result == null) {
            return "";
        }

        if (result.isUpdateAvailable()) {
            return ChatColor.GRAY + " (" + ChatColor.GREEN + ChatColor.BOLD + "UPDATE AVAILABLE!" + ChatColor.GRAY + ")";
        } else if (result.isUnreleased()) {
            return ChatColor.GRAY + " (" + ChatColor.AQUA + ChatColor.BOLD + "DEV BUILD!" + ChatColor.GRAY + ")";
        } else if (result.isFailed()) {
            return ChatColor.GRAY + " (" + ChatColor.RED + ChatColor.BOLD + "UPDATE CHECK FAILED!" + ChatColor.GRAY + ")";
        }

        return "";
    }

}
