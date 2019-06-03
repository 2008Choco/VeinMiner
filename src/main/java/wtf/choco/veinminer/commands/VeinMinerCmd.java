package wtf.choco.veinminer.commands;

import static wtf.choco.veinminer.VeinMiner.CHAT_PREFIX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
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
import wtf.choco.veinminer.utils.UpdateChecker;
import wtf.choco.veinminer.utils.UpdateChecker.UpdateResult;

public final class VeinMinerCmd implements TabExecutor {

    private final VeinMiner plugin;
    private final VeinMinerManager manager;

    public VeinMinerCmd(VeinMiner plugin) {
        this.plugin = plugin;
        this.manager = plugin.getVeinMinerManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/veinminer <reload|version|blocklist|toggle|pattern>");
            return true;
        }

        // Reload subcommand
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("veinminer.reload")) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                return true;
            }

            this.plugin.reloadConfig();
            this.manager.loadToolTemplates();
            this.manager.loadVeinableBlocks();
            this.manager.loadDisabledWorlds();
            this.manager.loadMaterialAliases();

            sender.sendMessage(CHAT_PREFIX + ChatColor.GREEN + "VeinMiner configuration successfully reloaded");
        }

        // Version subcommand
        else if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Version: " + ChatColor.RESET + ChatColor.GRAY + plugin.getDescription().getVersion() + getUpdateSuffix());
            sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Developer: " + ChatColor.RESET + ChatColor.GRAY + "2008Choco " + ChatColor.YELLOW + "( https://choco.gg )");
            sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Development page: " + ChatColor.RESET + ChatColor.GRAY + "https://www.spigotmc.org/resources/veinminer.12038");
            sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Report bugs to: " + ChatColor.RESET + ChatColor.GRAY + "https://github.com/2008Choco/VeinMiner/issues");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
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
                ToolCategory category = ToolCategory.getByName(args[1]);
                if (category == null) {
                    player.sendMessage(CHAT_PREFIX + "Invalid tool category: " + ChatColor.YELLOW + args[1]);
                    return true;
                }

                playerData.setVeinMinerEnabled(!playerData.isVeinMinerEnabled(), category);
                player.sendMessage(CHAT_PREFIX + "VeinMiner successfully toggled "
                    + (playerData.isVeinMinerDisabled(category) ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
                    + ChatColor.GRAY + " for tool " + ChatColor.YELLOW + category.getName().toLowerCase());
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
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " blocklist <tool> <add|remove|list>");
                return true;
            }

            ToolCategory category = ToolCategory.getByName(args[1]);

            if (category == null) {
                sender.sendMessage(CHAT_PREFIX + "Invalid tool category: " + ChatColor.YELLOW + args[1]);
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " <add|remove|list>");
                return true;
            }

            // /veinminer blocklist <tool> add
            if (args[2].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("veinminer.blocklist.add")) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " add <block>[[data]]");
                    return true;
                }

                VeinBlock block = VeinBlock.fromString(args[3].toLowerCase());
                if (block == null) {
                    sender.sendMessage(VeinMiner.CHAT_PREFIX + ChatColor.RED + "Unknown block type (was it an item?) and/or block states. Given: " + ChatColor.YELLOW + args[3].toLowerCase());
                    return true;
                }

                List<String> configBlocklist = plugin.getConfig().getStringList("BlockList." + category.getName());
                BlockList blocklist = manager.getBlockList(category);

                if (blocklist.contains(block)) {
                    sender.sendMessage(CHAT_PREFIX + "A block with the ID " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " is already on the " + ChatColor.YELLOW + args[1].toLowerCase() + ChatColor.GRAY + " blocklist");
                    return true;
                }

                blocklist.add(block);

                configBlocklist.add(block.asDataString());
                this.plugin.getConfig().set("BlockList." + category.getName(), configBlocklist);
                this.plugin.saveConfig();
                this.plugin.reloadConfig();

                sender.sendMessage(CHAT_PREFIX + "Block ID " + ChatColor.YELLOW + block.asDataString() + ChatColor.GRAY + " successfully added to the list");
            }

            // /veinminer blocklist <category> remove
            else if (args[2].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("veinminer.blocklist.remove")) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " remove <block>[[data]]");
                    return true;
                }

                VeinBlock block = VeinBlock.fromString(args[3].toLowerCase());
                if (block == null) {
                    sender.sendMessage(VeinMiner.CHAT_PREFIX + ChatColor.RED + "Unknown block type (was it an item?) and/or block states. Given: " + ChatColor.YELLOW + args[3].toLowerCase());
                    return true;
                }

                List<String> configBlocklist = plugin.getConfig().getStringList("BlockList." + category.getName());
                BlockList blocklist = manager.getBlockList(category);

                if (!blocklist.contains(block)) {
                    sender.sendMessage(CHAT_PREFIX + "No block with the ID " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " was found on the " + ChatColor.YELLOW + args[1].toLowerCase() + ChatColor.GRAY + " blocklist");
                    return true;
                }

                blocklist.remove(block);
                configBlocklist.remove(block.asDataString());
                this.plugin.getConfig().set("BlockList." + category.getName(), configBlocklist);
                this.plugin.saveConfig();
                this.plugin.reloadConfig();

                sender.sendMessage(CHAT_PREFIX + "Block ID " + ChatColor.YELLOW + block.asDataString() + ChatColor.GRAY + " successfully removed from the list");
            }

            // /veinminer blocklist <tool> list
            else if (args[2].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("veinminer.blocklist.list." + category.getName().toLowerCase())) {
                    sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
                    return true;
                }

                Iterable<VeinBlock> blocklistIterable;
                if (plugin.getConfig().getBoolean("SortBlocklistAlphabetically", true)) {
                    blocklistIterable = new LinkedList<>();
                    Iterables.addAll((List<VeinBlock>) blocklistIterable, manager.getBlockList(category));
                    ((LinkedList<VeinBlock>) blocklistIterable).sort(Comparator.comparing(veinBlock -> veinBlock.getType().getKey().getKey()));
                } else {
                    blocklistIterable = manager.getBlockList(category);
                }

                sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "VeinMiner Blocklist (Tool = " + category + "): ");
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
            sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Unknown parameter " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " <reload|version|blocklist|toggle|pattern>");
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
                for (ToolCategory category : ToolCategory.values()) {
                    values.add(category.name().toLowerCase());
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

        for (ToolCategory category : ToolCategory.values())
            if (player.hasPermission("veinminer.veinmine." + category.getName().toLowerCase())) return true;
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
