package wtf.choco.veinminer.commands;

import static wtf.choco.veinminer.VeinMiner.CHAT_PREFIX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.api.event.PlayerSwitchPatternEvent;
import wtf.choco.veinminer.data.BlockList;
import wtf.choco.veinminer.data.VMPlayerData;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.utils.Chat;
import wtf.choco.veinminer.utils.UpdateChecker;
import wtf.choco.veinminer.utils.UpdateChecker.UpdateResult;

public final class VeinMinerCmd implements TabExecutor {

    private final VeinMiner plugin;

    public VeinMinerCmd(VeinMiner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(CHAT_PREFIX + Chat.translate("%rInvalid command syntax! %gMissing parameter. %y/veinminer <reload|version|blocklist|toggle|pattern>", ChatColor.RED, ChatColor.GRAY, ChatColor.YELLOW));
            return true;
        }

        // Reload subcommand
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("veinminer.reload")) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                return true;
            }

            this.plugin.reloadConfig();
            ToolCategory.clearCategories();

            VeinMinerManager manager = plugin.getVeinMinerManager();
            manager.loadToolCategories();
            manager.loadVeinableBlocks();
            manager.loadMaterialAliases();

            sender.sendMessage(CHAT_PREFIX + ChatColor.GREEN + "VeinMiner configuration successfully reloaded");
        }

        // Version subcommand
        else if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(Chat.translate("%g%b%s--------------------------------------------", ChatColor.GOLD, ChatColor.BOLD, ChatColor.STRIKETHROUGH));
            sender.sendMessage("");
            sender.sendMessage(Chat.translate("%a%bVersion: %g", ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GRAY) + plugin.getDescription().getVersion() + getUpdateSuffix());
            sender.sendMessage(Chat.translate("%a%bDeveloper: %gChoco %y(https://choco.wtf)", ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GRAY, ChatColor.YELLOW));
            sender.sendMessage(Chat.translate("%a%bPlugin Page: %ghttps://www.spigotmc.org/resources/veinminer.12038", ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GRAY));
            sender.sendMessage(Chat.translate("%a%bReport bugs to: %ghttps://github.com/2008Choco/VeinMiner/issues", ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GRAY));
            sender.sendMessage("");
            sender.sendMessage(Chat.translate("%g%b%s--------------------------------------------", ChatColor.GOLD, ChatColor.BOLD, ChatColor.STRIKETHROUGH));
        }

        // Toggle subcommand
        else if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("VeinMiner cannot be toggled from the console...");
                return true;
            }

            Player player = (Player) sender;
            if (!canVeinMine(player)) {
                player.sendMessage(CHAT_PREFIX + ChatColor.RED + "You may not toggle a feature to which you do not have access");
                return true;
            }

            if (!player.hasPermission("veinminer.toggle")) {
                player.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                return true;
            }

            VMPlayerData playerData = VMPlayerData.get(player);
            // Toggle a specific tool
            if (args.length >= 2) {
                ToolCategory category = ToolCategory.get(args[1]);
                if (category == null) {
                    player.sendMessage(CHAT_PREFIX + "Invalid tool category: " + ChatColor.YELLOW + args[1]);
                    return true;
                }

                playerData.setVeinMinerEnabled(!playerData.isVeinMinerEnabled(), category);
                player.sendMessage(CHAT_PREFIX + "VeinMiner successfully toggled "
                    + (playerData.isVeinMinerDisabled(category) ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
                    + ChatColor.GRAY + " for tool " + ChatColor.YELLOW + category.getId().toLowerCase());
            }

            // Toggle all tools
            else {
                playerData.setVeinMinerEnabled(!playerData.isVeinMinerEnabled());
                player.sendMessage(CHAT_PREFIX + "VeinMiner successfully toggled "
                    + (playerData.isVeinMinerEnabled() ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
                    + ChatColor.GRAY + " for " + ChatColor.YELLOW + "all tools");
            }
        }

        // Blocklist subcommand
        else if (args[0].equalsIgnoreCase("blocklist")) {
            if (args.length < 2) {
                sender.sendMessage(CHAT_PREFIX + Chat.translate("%rInvalid command syntax! %gMissing parameter(s) %y/" + label + " blocklist <tool> <add|remove|list>", ChatColor.RED, ChatColor.GRAY, ChatColor.YELLOW));
                return true;
            }

            ToolCategory category = ToolCategory.get(args[1]);

            if (category == null) {
                sender.sendMessage(CHAT_PREFIX + "Invalid tool category: " + ChatColor.YELLOW + args[1]);
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(CHAT_PREFIX + Chat.translate("%rInvalid command syntax! %gMissing parameter(s) %y/" + label + " blocklist " + args[1] + " <add|remove|list>", ChatColor.RED, ChatColor.GRAY, ChatColor.YELLOW));
                return true;
            }

            // /veinminer blocklist <tool> add
            if (args[2].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("veinminer.blocklist.add")) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(CHAT_PREFIX + Chat.translate("%rInvalid command syntax! %gMissing parameter(s) %y/" + label + " blocklist " + args[1] + " add <block>[[data]]", ChatColor.RED, ChatColor.GRAY, ChatColor.YELLOW));
                    return true;
                }

                VeinBlock block = VeinBlock.fromString(args[3].toLowerCase());
                if (block == null) {
                    sender.sendMessage(VeinMiner.CHAT_PREFIX + Chat.translate("%rUnknown block type (was it an item?) and/or block states. Given %y" + args[3].toLowerCase(), ChatColor.RED, ChatColor.YELLOW));
                    return true;
                }

                List<String> configBlocklist = plugin.getConfig().getStringList("BlockList." + category.getId());
                BlockList blocklist = category.getBlocklist();

                if (blocklist.contains(block)) {
                    sender.sendMessage(CHAT_PREFIX + Chat.translate("A block with the ID %y" + args[3] + " %gis already on the %y" + args[1].toLowerCase() + " %gblocklist", ChatColor.YELLOW, ChatColor.GRAY));
                    return true;
                }

                blocklist.add(block);

                configBlocklist.add(block.asDataString());
                this.plugin.getConfig().set("BlockList." + category.getId(), configBlocklist);
                this.plugin.saveConfig();
                this.plugin.reloadConfig();

                sender.sendMessage(CHAT_PREFIX + Chat.translate("Block ID %y" + block.asDataString() + " %gsuccessfully added to the blocklist", ChatColor.YELLOW, ChatColor.GRAY));
            }

            // /veinminer blocklist <category> remove
            else if (args[2].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("veinminer.blocklist.remove")) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(CHAT_PREFIX + Chat.translate("%rInvalid command syntax! %gMissing parameter %y/" + label + " blocklist " + args[1] + " remove <block>[[data]]", ChatColor.RED, ChatColor.GRAY, ChatColor.YELLOW));
                    return true;
                }

                VeinBlock block = VeinBlock.fromString(args[3].toLowerCase());
                if (block == null) {
                    sender.sendMessage(VeinMiner.CHAT_PREFIX + Chat.translate("%rUnknown block type (was it an item?) and/or block states. Given %y" + args[3].toLowerCase(), ChatColor.RED, ChatColor.YELLOW));
                     return true;
                }

                List<String> configBlocklist = plugin.getConfig().getStringList("BlockList." + category.getId());
                BlockList blocklist = category.getBlocklist();

                if (!blocklist.contains(block)) {
                    sender.sendMessage(CHAT_PREFIX + Chat.translate("No block with the ID %y" + args[3] + " %gwas found on the %y" + args[1].toLowerCase() + " %gblocklist", ChatColor.YELLOW, ChatColor.GRAY));
                    return true;
                }

                blocklist.remove(block);
                configBlocklist.remove(block.asDataString());
                this.plugin.getConfig().set("BlockList." + category.getId(), configBlocklist);
                this.plugin.saveConfig();
                this.plugin.reloadConfig();

                sender.sendMessage(CHAT_PREFIX + Chat.translate("Block ID %y" + block.asDataString() + " %gsuccessfully removed from the blocklist", ChatColor.YELLOW, ChatColor.GRAY));
            }

            // /veinminer blocklist <tool> list
            else if (args[2].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("veinminer.blocklist.list." + category.getId().toLowerCase())) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                    return true;
                }

                Iterable<VeinBlock> blocklistIterable;
                if (plugin.getConfig().getBoolean("SortBlocklistAlphabetically", true)) {
                    blocklistIterable = new ArrayList<>();
                    Iterables.addAll((List<VeinBlock>) blocklistIterable, category.getBlocklist());
                    Collections.sort((List<VeinBlock>) blocklistIterable);
                } else {
                    blocklistIterable = category.getBlocklist();
                }

                sender.sendMessage(Chat.translate("%y%bVeinMiner Blocklist (Category = " + category.getId() + ")", ChatColor.YELLOW, ChatColor.BOLD));
                blocklistIterable.forEach(block -> sender.sendMessage(ChatColor.YELLOW + "  - " + block.asDataString()));
            }

            // Unknown parameter
            else {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Unknown parameter " + ChatColor.AQUA + args[2] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " <add|remove|list>");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("pattern")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("VeinMiner patterns cannot be changed from the console...");
                return true;
            }

            if (!sender.hasPermission("veinminer.pattern")) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " pattern <pattern_id>");
                return true;
            }

            Player player = (Player) sender;
            String patternNamespace = args[1].toLowerCase();

            if (!patternNamespace.contains(":")) {
                patternNamespace = plugin.getName().toLowerCase() + ":" + patternNamespace;
            } else if (patternNamespace.startsWith(":") || patternNamespace.split(":").length > 2) {
                player.sendMessage(CHAT_PREFIX + "Invalid ID. Pattern IDs should be formatted as " + ChatColor.YELLOW + "namespace:id" + ChatColor.GRAY + " (i.e. " + ChatColor.YELLOW + "veinminer:expansive" + ChatColor.GRAY + ")");
                return true;
            }

            VeinMiningPattern pattern = plugin.getPatternRegistry().getPattern(patternNamespace);
            if (pattern == null) {
                player.sendMessage(CHAT_PREFIX + "A pattern with the ID " + ChatColor.YELLOW + patternNamespace + ChatColor.GRAY + " could not be found");
                return true;
            }

            VMPlayerData playerData = VMPlayerData.get(player);
            PlayerSwitchPatternEvent pspe = new PlayerSwitchPatternEvent(player, playerData.getPattern(), pattern);
            Bukkit.getPluginManager().callEvent(pspe);

            playerData.setPattern(pattern);
            player.sendMessage(CHAT_PREFIX + "Pattern successfully changed to " + ChatColor.YELLOW + patternNamespace);
        }

        // Unknown command usage
        else {
            sender.sendMessage(CHAT_PREFIX + Chat.translate("%rInvalid command syntax! %gUnknown parameter, %a" + args[0] + "%g. %y/" + label + " <reload|version|blocklist|toggle|pattern>", ChatColor.RED, ChatColor.GRAY, ChatColor.AQUA, ChatColor.YELLOW));
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        List<String> values = new ArrayList<>();
        if (args.length == 1) {
            values.add("version");
            if (sender.hasPermission("veinminer.reload")) {
                values.add("reload");
            }
            if (sender.hasPermission("veinminer.toggle")) {
                values.add("toggle");
            }
            if (hasBlocklistPerms(sender)) {
                values.add("blocklist");
            }
            if (sender.hasPermission("veinminer.pattern")) {
                values.add("pattern");
            }
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("blocklist")) {
                for (ToolCategory category : ToolCategory.getAll()) {
                    values.add(category.getId().toLowerCase());
                }
            }

            else if (args[0].equalsIgnoreCase("pattern")) {
                values = plugin.getPatternRegistry().getPatterns().stream()
                        .map(VeinMiningPattern::getKey).map(NamespacedKey::toString)
                        .collect(Collectors.toList());
            }
        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("blocklist")) {
                if (sender.hasPermission("veinminer.blocklist.add")) {
                    values.add("add");
                }
                if (sender.hasPermission("veinminer.blocklist.remove")) {
                    values.add("remove");
                }
                if (sender.hasPermission("veinminer.blocklist.list.*")) {
                    values.add("list");
                }
            }
        }
        else {
            return null;
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], values, new ArrayList<>());
    }

    public void assignTo(@Nullable PluginCommand command) {
        if (command == null) return;

        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    private boolean hasBlocklistPerms(CommandSender sender) {
        return sender.hasPermission("veinminer.blocklist.add")
            || sender.hasPermission("veinminer.blocklist.remove")
            || sender.hasPermission("veinminer.blocklist.list.*");
    }

    private boolean canVeinMine(Player player) {
        if (player.hasPermission("veinminer.veinmine.*")) return true;

        for (ToolCategory category : ToolCategory.getAll())
            if (player.hasPermission("veinminer.veinmine." + category.getId().toLowerCase())) return true;
        return false;
    }

    private String getUpdateSuffix() {
        if (!plugin.getConfig().getBoolean("PerformUpdateChecks")) {
            return "";
        }

        UpdateResult result = UpdateChecker.get().getLastResult();
        return (result != null && result.requiresUpdate()) ? " (" + ChatColor.GREEN + ChatColor.BOLD + "UPDATE AVAILABLE!" + ChatColor.GRAY + ")" : "";
    }

}
