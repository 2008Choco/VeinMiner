package wtf.choco.veinminer.command;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import wtf.choco.veinminer.network.VeinMinerPlayer;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.NamespacedKey;
import wtf.choco.veinminer.util.UpdateChecker;
import wtf.choco.veinminer.util.UpdateChecker.UpdateResult;
import wtf.choco.veinminer.util.VMConstants;

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
            sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " <version|reload|blocklist|toollist|toggle|pattern|mode>");
            return true;
        }

        // Reload subcommand
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
        }

        // Version subcommand
        else if (args[0].equalsIgnoreCase("version")) {
            PluginDescriptionFile description = plugin.getDescription();
            String headerFooter = ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 44);

            sender.sendMessage(headerFooter);
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.WHITE + description.getVersion() + getUpdateSuffix());
            sender.sendMessage(ChatColor.GOLD + "Developer: " + ChatColor.WHITE + description.getAuthors().get(0));
            sender.sendMessage(ChatColor.GOLD + "Plugin page: " + ChatColor.WHITE + description.getWebsite());
            sender.sendMessage(ChatColor.GOLD + "Report bugs to: " + ChatColor.WHITE + "https://github.com/2008Choco/VeinMiner/issues");
            sender.sendMessage("");
            sender.sendMessage(headerFooter);
        }

        // Toggle subcommand
        else if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("VeinMiner cannot be toggled from the console.");
                return true;
            }

            if (!canVeinMine(player)) {
                player.sendMessage(ChatColor.RED + "You may not toggle a feature to which you do not have access.");
                return true;
            }

            if (!player.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE)) {
                player.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            // Toggle a specific tool
            if (args.length >= 2) {
                VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
                if (category == null) {
                    player.sendMessage(ChatColor.GRAY + "Invalid tool category: " + ChatColor.YELLOW + args[1]);
                    return true;
                }

                veinMinerPlayer.setVeinMinerEnabled(!veinMinerPlayer.isVeinMinerEnabled(), category);
                player.sendMessage(ChatColor.GRAY + "VeinMiner successfully toggled "
                    + (veinMinerPlayer.isVeinMinerDisabled(category) ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
                    + ChatColor.GRAY + " for tool " + ChatColor.YELLOW + category.getId().toLowerCase() + ChatColor.GRAY + ".");
            }

            // Toggle all tools
            else {
                veinMinerPlayer.setVeinMinerEnabled(!veinMinerPlayer.isVeinMinerEnabled());
                player.sendMessage(ChatColor.GRAY + "VeinMiner successfully toggled "
                    + (veinMinerPlayer.isVeinMinerEnabled() ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
                    + ChatColor.GRAY + " for " + ChatColor.YELLOW + "all tools");
            }
        }

        // Mode subcommand
        else if (args[0].equalsIgnoreCase("mode")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("VeinMiner cannot change mode from the console.");
                return true;
            }

            if (!canVeinMine(player)) {
                player.sendMessage(ChatColor.RED + "You may not toggle a feature to which you do not have access.");
                return true;
            }

            if (!player.hasPermission(VMConstants.PERMISSION_COMMAND_MODE)) {
                player.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <sneak|stand|always|client>");
                return true;
            }

            Optional<ActivationStrategy> strategyOptional = Enums.getIfPresent(ActivationStrategy.class, args[1].toUpperCase());
            if (!strategyOptional.isPresent()) {
                player.sendMessage(ChatColor.GRAY + "Invalid activation strategy: " + ChatColor.YELLOW + args[1] + ChatColor.GRAY + ".");
                return true;
            }

            ActivationStrategy strategy = strategyOptional.get();
            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);

            if (strategy == ActivationStrategy.CLIENT && !veinMinerPlayer.isUsingClientMod()) {
                player.sendMessage(ChatColor.RED + "You do not have VeinMiner installed on your client!");

                // Let them know where to install VeinMiner on the client (if it's allowed)
                if (plugin.getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
                    player.sendMessage("In order to use client activation, you must install a client-sided mod.");
                    player.sendMessage("https://www.curseforge.com/minecraft/mc-mods/veinminer4bukkit");
                    player.sendMessage("Supports " + ChatColor.GRAY + "Fabric" + ChatColor.RESET + " (support for " + ChatColor.GRAY + "Forge" + ChatColor.RESET + " Soon™)");
                }

                return true;
            }

            veinMinerPlayer.setActivationStrategy(strategy);
            player.sendMessage(ChatColor.GREEN + "Activation mode successfully changed to " + ChatColor.YELLOW + strategy.name().toLowerCase().replace("_", " ") + ChatColor.GREEN + ".");
        }

        // Blocklist subcommand
        else if (args[0].equalsIgnoreCase("blocklist")) {
            this.commandBlocklist.execute(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            this.commandToollist.execute(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args[0].equalsIgnoreCase("pattern")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "The console cannot change its vein miner pattern.");
                return true;
            }

            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <pattern_id>");
                return true;
            }

            org.bukkit.NamespacedKey patternKey = org.bukkit.NamespacedKey.fromString(args[1], plugin);
            if (patternKey == null) {
                sender.sendMessage(ChatColor.RED + "Invalid pattern ID! " + ChatColor.GRAY + "Pattern IDs should be formatted as " + ChatColor.YELLOW + "namespace:id" + ChatColor.GRAY + "(i.e. " + ChatColor.YELLOW + "veinminer:expansive" + ChatColor.GRAY + ").");
                return true;
            }

            VeinMiningPattern pattern = plugin.getPatternRegistry().get(new NamespacedKey(patternKey.getNamespace(), patternKey.getKey()));
            if (pattern == null) {
                sender.sendMessage(ChatColor.GRAY + "A pattern with the ID " + ChatColor.YELLOW + patternKey + ChatColor.GRAY + " could not be found.");
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            veinMinerPlayer.setVeinMiningPattern(pattern);
            this.plugin.getConfig().set(VMConstants.CONFIG_DEFAULT_VEIN_MINING_PATTERN, pattern.getKey().toString());
            this.plugin.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "Pattern successfully set to " + ChatColor.YELLOW + patternKey + ChatColor.GRAY + ".");
        }

        // Unknown command usage
        else {
            sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Unknown parameter, " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " <version|reload|blocklist|toollist|toggle|pattern|mode>");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String @NotNull [] args) {
        List<String> values = new ArrayList<>();

        if (args.length == 1) {
            values.add("version");
            if (sender.hasPermission(VMConstants.PERMISSION_COMMAND_RELOAD)) {
                values.add("reload");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_COMMAND_BLOCKLIST)) {
                values.add("blocklist");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOOLLIST)) {
                values.add("toollist");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE)) {
                values.add("toggle");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_COMMAND_MODE)) {
                values.add("mode");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN)) {
                values.add("pattern");
            }

            return StringUtil.copyPartialMatches(args[0], values, new ArrayList<>());
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("blocklist")) {
            return commandBlocklist.tabComplete(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args.length > 1 && args[0].equalsIgnoreCase("toollist")) {
            return commandToollist.tabComplete(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle")) {
                this.plugin.getToolCategoryRegistry().getAll().forEach(category -> values.add(category.getId().toLowerCase()));
            }

            else if (args[0].equalsIgnoreCase("mode")) {
                for (ActivationStrategy activationStrategy : ActivationStrategy.values()) {
                    if (activationStrategy == ActivationStrategy.CLIENT && !plugin.getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
                        continue;
                    }

                    values.add(activationStrategy.name().toLowerCase());
                }
            }

            else if (args[0].equalsIgnoreCase("pattern")) {
                for (VeinMiningPattern pattern : plugin.getPatternRegistry().getPatterns()) {
                    String patternKey = pattern.getKey().toString();
                    if (patternKey.contains(args[1])) {
                        values.add(patternKey);
                    }
                }
            }
        }

        else {
            return Collections.emptyList();
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], values, new ArrayList<>());
    }

    private boolean canVeinMine(Player player) {
        for (VeinMinerToolCategory category : plugin.getToolCategoryRegistry().getAll()) {
            if (player.hasPermission(String.format(VMConstants.PERMISSION_DYNAMIC_VEINMINE, category.getId().toLowerCase()))) {
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
