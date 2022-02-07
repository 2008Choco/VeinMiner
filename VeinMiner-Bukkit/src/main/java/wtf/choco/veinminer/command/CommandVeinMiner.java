package wtf.choco.veinminer.command;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.event.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.network.VeinMinerPlayer;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.NamespacedKey;
import wtf.choco.veinminer.util.UpdateChecker;
import wtf.choco.veinminer.util.UpdateChecker.UpdateResult;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

public final class CommandVeinMiner implements TabExecutor {

    private final VeinMinerPlugin plugin;
    private final PluginCommand commandBlocklist;
    private final PluginCommand commandToollist;

    public CommandVeinMiner(@NotNull VeinMinerPlugin plugin, PluginCommand commandBlocklist, PluginCommand commandToollist) {
        this.plugin = plugin;
        this.commandBlocklist = commandBlocklist;
        this.commandToollist = commandToollist;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
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

            this.plugin.reloadGeneralConfig();
            this.plugin.reloadVeinMinerManagerConfig();
            this.plugin.reloadToolCategoryRegistryConfig();

            sender.sendMessage(ChatColor.GREEN + "VeinMiner configuration successfully reloaded.");
            return true;
        }

        else if (args[0].equalsIgnoreCase("version")) {
            PluginDescriptionFile description = plugin.getDescription();
            String headerFooter = ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 44);

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

            // Toggle a specific tool
            if (args.length >= 2) {
                VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
                if (category == null) {
                    player.sendMessage(ChatColor.RED + "Invalid tool category " + args[1] + ".");
                    return true;
                }

                veinMinerPlayer.setVeinMinerEnabled(!veinMinerPlayer.isVeinMinerEnabled(), category);
                player.sendMessage(ChatColor.GRAY + "Vein miner toggled "
                    + (veinMinerPlayer.isVeinMinerDisabled(category)
                            ? ChatColor.RED.toString() + ChatColor.BOLD + "OFF"
                            : ChatColor.GREEN.toString() + ChatColor.BOLD + "ON"
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
                return false;
            }

            Optional<ActivationStrategy> strategyOptional = Enums.getIfPresent(ActivationStrategy.class, args[1].toUpperCase());
            if (!strategyOptional.isPresent()) {
                player.sendMessage(ChatColor.RED + "Invalid mode " + args[1] + ".");
                return true;
            }

            ActivationStrategy strategy = strategyOptional.get();
            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);

            if (strategy == ActivationStrategy.CLIENT && !veinMinerPlayer.isUsingClientMod()) {
                player.sendMessage(ChatColor.RED + "You do not have VeinMiner4Bukkit installed on your client!");

                // Let them know where to install VeinMiner on the client (if it's allowed)
                if (plugin.getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
                    player.sendMessage("In order to use client activation, you must install a client-sided mod.");
                    player.sendMessage("https://www.curseforge.com/minecraft/mc-mods/veinminer4bukkit");
                    player.sendMessage("Supports " + ChatColor.GRAY + "Fabric" + ChatColor.RESET + " (support for " + ChatColor.GRAY + "Forge" + ChatColor.RESET + " Soon™)");
                }

                return true;
            }

            veinMinerPlayer.setActivationStrategy(strategy);
            player.sendMessage(ChatColor.GREEN + "Mode successfully changed to " + ChatColor.YELLOW + strategy.name().toLowerCase().replace("_", " ") + ChatColor.GREEN + ".");
            return true;
        }

        else if (args[0].equalsIgnoreCase("blocklist")) {
            this.commandBlocklist.execute(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            this.commandToollist.execute(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
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
                return false;
            }

            org.bukkit.NamespacedKey patternKey = org.bukkit.NamespacedKey.fromString(args[1], plugin);
            if (patternKey == null) {
                sender.sendMessage(ChatColor.RED + "Invalid pattern key " + args[1] + ".");
                return true;
            }

            VeinMiningPattern pattern = plugin.getPatternRegistry().get(new NamespacedKey(patternKey.getNamespace(), patternKey.getKey()));
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
            PlayerVeinMiningPatternChangeEvent event = VMEventFactory.callPlayerVeinMiningPatternChangeEvent(player, veinMinerPlayer.getVeinMiningPattern(), pattern);

            if (event.isCancelled()) {
                return true;
            }

            pattern = event.getNewPattern();
            veinMinerPlayer.setVeinMiningPattern(pattern);

            sender.sendMessage(ChatColor.GREEN + "Pattern set to " + pattern.getKey() + ".");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            suggestions.add("version");
            this.addConditionally(suggestions, "reload", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_RELOAD));
            this.addConditionally(suggestions, "blocklist", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_BLOCKLIST));
            this.addConditionally(suggestions, "toollist", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOOLLIST));
            this.addConditionally(suggestions, "toggle", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE));
            this.addConditionally(suggestions, "mode", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_MODE));
            this.addConditionally(suggestions, "pattern", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN));

            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        if (args[0].equalsIgnoreCase("blocklist")) {
            return commandBlocklist.tabComplete(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            return commandToollist.tabComplete(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();

            if (args[0].equalsIgnoreCase("toggle")) {
                this.plugin.getToolCategoryRegistry().getAll().forEach(category -> suggestions.add(category.getId().toLowerCase()));
            }

            else if (args[0].equalsIgnoreCase("mode")) {
                for (ActivationStrategy activationStrategy : ActivationStrategy.values()) {
                    if (activationStrategy == ActivationStrategy.CLIENT && !plugin.getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
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
        if (!plugin.getConfig().getBoolean(VMConstants.CONFIG_PERFORM_UPDATE_CHECKS, true)) {
            return "";
        }

        UpdateResult result = UpdateChecker.get().getLastResult();
        return (result != null && result.isUpdateAvailable()) ? " (" + ChatColor.GREEN + ChatColor.BOLD + "UPDATE AVAILABLE!" + ChatColor.GRAY + ")" : "";
    }

}
