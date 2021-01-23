package wtf.choco.veinminer.commands;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.data.BlockList;
import wtf.choco.veinminer.data.PlayerPreferences;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplateMaterial;
import wtf.choco.veinminer.utils.ConfigWrapper;
import wtf.choco.veinminer.utils.UpdateChecker;
import wtf.choco.veinminer.utils.UpdateChecker.UpdateResult;
import wtf.choco.veinminer.utils.VMConstants;

public final class VeinMinerCommand implements TabExecutor {

    private static final List<String> BLOCK_KEYS = Arrays.stream(Material.values()).filter(Material::isBlock).map(m -> m.getKey().toString()).collect(Collectors.toList());
    private static final List<String> ITEM_KEYS = Arrays.stream(Material.values()).filter(Material::isItem).map(m -> m.getKey().toString()).collect(Collectors.toList());

    private final VeinMiner plugin;

    public VeinMinerCommand(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " <version|reload|blocklist|toollist|toggle|pattern|mode>");
            return true;
        }

        // Reload subcommand
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_RELOAD)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            this.plugin.reloadConfig();
            this.plugin.getCategoriesConfig().reload();

            // Clear data from memory
            ToolCategory.clearCategories();
            VeinMinerManager manager = plugin.getVeinMinerManager();
            manager.clearLocalisedData();

            // Load data into memory
            manager.loadToolCategories();
            manager.loadVeinableBlocks();
            manager.loadMaterialAliases();
            manager.loadDisabledGameModes();

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
            if (!(sender instanceof Player)) {
                sender.sendMessage("VeinMiner cannot be toggled from the console...");
                return true;
            }

            Player player = (Player) sender;
            if (!canVeinMine(player)) {
                player.sendMessage(ChatColor.RED + "You may not toggle a feature to which you do not have access.");
                return true;
            }

            if (!player.hasPermission(VMConstants.PERMISSION_TOGGLE)) {
                player.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            PlayerPreferences playerData = PlayerPreferences.get(player);
            // Toggle a specific tool
            if (args.length >= 2) {
                ToolCategory category = ToolCategory.get(args[1]);
                if (category == null) {
                    player.sendMessage(ChatColor.GRAY + "Invalid tool category: " + ChatColor.YELLOW + args[1]);
                    return true;
                }

                playerData.setVeinMinerEnabled(!playerData.isVeinMinerEnabled(), category);
                player.sendMessage(ChatColor.GRAY + "VeinMiner successfully toggled "
                    + (playerData.isVeinMinerDisabled(category) ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
                    + ChatColor.GRAY + " for tool " + ChatColor.YELLOW + category.getId().toLowerCase() + ChatColor.GRAY + ".");
            }

            // Toggle all tools
            else {
                playerData.setVeinMinerEnabled(!playerData.isVeinMinerEnabled());
                player.sendMessage(ChatColor.GRAY + "VeinMiner successfully toggled "
                    + (playerData.isVeinMinerEnabled() ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
                    + ChatColor.GRAY + " for " + ChatColor.YELLOW + "all tools");
            }
        }

        // Mode subcommand
        else if (args[0].equalsIgnoreCase("mode")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("VeinMiner cannot change mode from the console...");
                return true;
            }

            Player player = (Player) sender;
            if (!canVeinMine(player)) {
                player.sendMessage(ChatColor.RED + "You may not toggle a feature to which you do not have access.");
                return true;
            }

            if (!player.hasPermission(VMConstants.PERMISSION_MODE)) {
                player.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <sneak|stand|always>");
                return true;
            }

            Optional<@NotNull ActivationStrategy> strategyOptional = Enums.getIfPresent(ActivationStrategy.class, args[1].toUpperCase());
            if (!strategyOptional.isPresent()) {
                player.sendMessage(ChatColor.GRAY + "Invalid activation strategy: " + ChatColor.YELLOW + args[1] + ChatColor.GRAY + ".");
                return true;
            }

            ActivationStrategy strategy = strategyOptional.get();
            PlayerPreferences.get(player).setActivationStrategy(strategy);
            player.sendMessage(ChatColor.GREEN + "Activation mode successfully changed to " + ChatColor.YELLOW + strategy.name().toLowerCase().replace("_", " ") + ChatColor.GREEN + ".");
        }

        // Blocklist subcommand
        else if (args[0].equalsIgnoreCase("blocklist")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <category> <add|remove|list>");
                return true;
            }

            ToolCategory category = ToolCategory.get(args[1]);
            if (category == null) {
                sender.sendMessage(ChatColor.GRAY + "Invalid tool category: " + ChatColor.YELLOW + args[1] + ChatColor.GRAY + ".");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " <add|remove|list>");
                return true;
            }

            // /veinminer blocklist <category> add
            if (args[2].equalsIgnoreCase("add")) {
                if (!sender.hasPermission(VMConstants.PERMISSION_BLOCKLIST_ADD)) {
                    sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " add <block>[data]");
                    return true;
                }

                BlockList blocklist = category.getBlocklist();
                List<String> configBlocklist = plugin.getConfig().getStringList("BlockList." + category.getId());

                for (int i = 3; i < args.length; i++) {
                    String blockArg = args[i].toLowerCase();
                    VeinBlock block = VeinBlock.fromString(blockArg);
                    if (block == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown block type/block state (was it an item)? " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + blockArg + ChatColor.GRAY + ".");
                        continue;
                    }

                    if (blocklist.contains(block)) {
                        sender.sendMessage(ChatColor.GRAY + "A block with the ID " + ChatColor.YELLOW + blockArg + ChatColor.GRAY + " is already on the " + ChatColor.YELLOW + category.getId() + " " + ChatColor.GRAY + " blocklist.");
                        continue;
                    }

                    blocklist.add(block);
                    configBlocklist.add(block.asDataString());
                    this.plugin.getConfig().set("BlockList." + category.getId(), configBlocklist);

                    sender.sendMessage(ChatColor.GRAY + "Block ID " + formatBlockData(block.asDataString()) + ChatColor.GRAY + " successfully added to the blocklist.");
                }

                this.plugin.saveConfig();
            }

            // /veinminer blocklist <category> remove
            else if (args[2].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission(VMConstants.PERMISSION_BLOCKLIST_REMOVE)) {
                    sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " add  <block>[data]");
                    return true;
                }

                BlockList blocklist = category.getBlocklist();
                List<String> configBlocklist = plugin.getConfig().getStringList("BlockList." + category.getId());

                for (int i = 3; i < args.length; i++) {
                    String blockArg = args[i].toLowerCase();
                    VeinBlock block = VeinBlock.fromString(blockArg);
                    if (block == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown block type/block state (was it an item)? " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + blockArg);
                        continue;
                    }

                    if (!blocklist.contains(block)) {
                        sender.sendMessage(ChatColor.GRAY + "No block with the ID " + ChatColor.YELLOW + blockArg + ChatColor.GRAY + " was found on the " + ChatColor.YELLOW + category.getId() + ChatColor.GRAY + " blocklist.");
                        continue;
                    }

                    blocklist.remove(block);
                    configBlocklist.remove(block.asDataString());
                    this.plugin.getConfig().set("BlockList." + category.getId(), configBlocklist);

                    sender.sendMessage(ChatColor.GRAY + "Block ID " + formatBlockData(block.asDataString()) + ChatColor.GRAY + " successfully removed from the blocklist.");
                }

                this.plugin.saveConfig();
            }

            // /veinminer blocklist <category> list
            else if (args[2].equalsIgnoreCase("list")) {
                if (!sender.hasPermission(VMConstants.PERMISSION_BLOCKLIST_LIST + "." + category.getId().toLowerCase())) {
                    sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                    return true;
                }

                Iterable<VeinBlock> blocklistIterable;
                if (plugin.getConfig().getBoolean(VMConstants.CONFIG_SORT_BLOCKLIST_ALPHABETICALLY, true)) {
                    blocklistIterable = new ArrayList<>();
                    Iterables.addAll((List<VeinBlock>) blocklistIterable, category.getBlocklist());
                    Collections.sort((List<VeinBlock>) blocklistIterable);
                } else {
                    blocklistIterable = category.getBlocklist();
                }

                if (Iterables.isEmpty(blocklistIterable)) {
                    sender.sendMessage(ChatColor.YELLOW + "The " + category.getId() + " category is empty.");
                    return true;
                }

                sender.sendMessage("");
                sender.sendMessage(ChatColor.GREEN + "Block list " + ChatColor.GRAY + "for category " + ChatColor.GREEN + category.getId().toLowerCase().replace("_", " ") + ChatColor.GRAY + ":");
                blocklistIterable.forEach(block -> sender.sendMessage(ChatColor.WHITE + " - " + formatBlockData(block.asDataString())));
                sender.sendMessage("");
            }

            // Unknown parameter
            else {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax!" + ChatColor.GRAY + " Unknown parameter: " + ChatColor.AQUA + args[2] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " <add|remove|list>");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <category> <add|remove|list>");
                return true;
            }

            ToolCategory category = ToolCategory.get(args[1]);
            if (category == null) {
                sender.sendMessage(ChatColor.GRAY + "Invalid tool category: " + ChatColor.YELLOW + args[1] + ChatColor.GRAY + ".");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " <add|remove|list>");
                return true;
            }

            // /veinminer toollist <category> add
            if (args[2].equalsIgnoreCase("add")) {
                if (!sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_ADD)) {
                    sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                    return true;
                }

                if (category == ToolCategory.HAND) {
                    sender.sendMessage(ChatColor.RED + "The hand category cannot be modified");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " add <item>");
                    return true;
                }

                ConfigWrapper categoriesConfigWrapper = plugin.getCategoriesConfig();
                FileConfiguration categoriesConfig = categoriesConfigWrapper.asRawConfig();
                @SuppressWarnings("unchecked")
                List<Object> configToolList = (List<Object>) categoriesConfig.getList(category.getId() + ".Items", new ArrayList<>());
                if (configToolList == null) {
                    sender.sendMessage(ChatColor.RED + "Something went wrong... is the " + ChatColor.YELLOW + "categories.yml " + ChatColor.GRAY + "formatted properly?");
                    return true;
                }

                for (int i = 3; i < args.length; i++) {
                    String toolArg = args[i].toLowerCase();
                    Material tool = Material.matchMaterial(toolArg);
                    if (tool == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown item. " + ChatColor.GRAY + "Given: " + ChatColor.YELLOW + toolArg + ChatColor.GRAY + ".");
                        continue;
                    }

                    if (category.containsTool(tool)) {
                        sender.sendMessage(ChatColor.GRAY + "An item with the ID " + ChatColor.YELLOW + toolArg + ChatColor.GRAY + " is already on the " + ChatColor.YELLOW + category.getId() + ChatColor.GRAY + " tool list.");
                        continue;
                    }

                    configToolList.add(tool.getKey().toString());
                    category.addTool(new ToolTemplateMaterial(category, tool));
                    categoriesConfig.set(category.getId() + ".Items", configToolList);

                    sender.sendMessage(ChatColor.GRAY + "Item ID " + ChatColor.YELLOW + tool.getKey() + ChatColor.GRAY + " successfully added to the " + ChatColor.YELLOW + category.getId() + ChatColor.GRAY + " tool list.");
                }

                categoriesConfigWrapper.save();
                categoriesConfigWrapper.reload();
            }

            // /veinminer toollist <category> remove
            else if (args[2].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_REMOVE)) {
                    sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                    return true;
                }

                if (category == ToolCategory.HAND) {
                    sender.sendMessage(ChatColor.RED + "The hand category cannot be modified");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " " + args[0] + " " + args[1] + " remove <item>");
                    return true;
                }

                ConfigWrapper categoriesConfigWrapper = plugin.getCategoriesConfig();
                FileConfiguration categoriesConfig = categoriesConfigWrapper.asRawConfig();
                @SuppressWarnings("unchecked")
                List<Object> configToolList = (List<Object>) categoriesConfig.getList(category.getId() + ".Items", new ArrayList<>());
                if (configToolList == null) {
                    sender.sendMessage(ChatColor.RED + "Something went wrong... is the " + ChatColor.YELLOW + "categories.yml " + ChatColor.GRAY + "formatted properly?");
                    return true;
                }

                for (int i = 3; i < args.length; i++) {
                    String toolArg = args[i].toLowerCase();
                    Material tool = Material.matchMaterial(toolArg);
                    if (tool == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown item. " + ChatColor.GRAY + "Given: " + ChatColor.YELLOW + toolArg + ChatColor.GRAY + ".");
                        continue;
                    }

                    if (!category.containsTool(tool)) {
                        sender.sendMessage(ChatColor.GRAY + "An item with the ID " + ChatColor.YELLOW + toolArg + ChatColor.GRAY + " is not on the " + ChatColor.YELLOW + category.getId() + ChatColor.GRAY + " tool list.");
                        continue;
                    }

                    configToolList.remove(tool.getKey().toString());
                    category.removeTool(tool);
                    categoriesConfig.set(category.getId() + ".Items", configToolList);

                    sender.sendMessage(ChatColor.GRAY + "Item ID " + ChatColor.YELLOW + tool.getKey() + ChatColor.GRAY + " successfully removed from the " + ChatColor.YELLOW + category.getId() + ChatColor.GRAY + " tool list.");
                }

                categoriesConfigWrapper.save();
                categoriesConfigWrapper.reload();
            }

            // /veinminer toollist <category> list
            else if (args[2].equalsIgnoreCase("list")) {
                if (!sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_LIST + "." + category.getId().toLowerCase())) {
                    sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                    return true;
                }

                sender.sendMessage("");
                sender.sendMessage(ChatColor.GREEN + "Tool list " + ChatColor.GRAY + "for category " + ChatColor.GREEN + category.getId().toLowerCase().replace("_", " ") + ChatColor.GRAY + ":");
                category.getTools().forEach(tool -> sender.sendMessage(ChatColor.WHITE + " - " + ChatColor.YELLOW + tool));
                sender.sendMessage("");
            }
        }

        else if (args[0].equalsIgnoreCase("pattern")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("VeinMiner patterns cannot be changed from the console...");
                return true;
            }

            if (!sender.hasPermission(VMConstants.PERMISSION_PATTERN)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <pattern_id>");
                return true;
            }

            Player player = (Player) sender;
            String patternNamespace = args[1].toLowerCase();

            if (!patternNamespace.contains(":")) {
                patternNamespace = plugin.getName().toLowerCase() + ":" + patternNamespace;
            } else if (patternNamespace.startsWith(":") || patternNamespace.split(":").length > 2) {
                player.sendMessage(ChatColor.RED + "Invalid pattern ID! " + ChatColor.GRAY + "Pattern IDs should be formatted as " + ChatColor.YELLOW + "namespace:id" + ChatColor.GRAY + "(i.e. " + ChatColor.YELLOW + "veinminer:expansive" + ChatColor.GRAY + ").");
                return true;
            }

            VeinMiningPattern pattern = plugin.getPatternRegistry().getPattern(patternNamespace);
            if (pattern == null) {
                player.sendMessage(ChatColor.GRAY + "A pattern with the ID " + ChatColor.YELLOW + patternNamespace + ChatColor.GRAY + " could not be found.");
                return true;
            }

            this.plugin.setVeinMiningPattern(pattern);
            player.sendMessage(ChatColor.GREEN + "Patterns successfully set to " + ChatColor.YELLOW + patternNamespace + ChatColor.GRAY + ".");
        }

        // Unknown command usage
        else {
            sender.sendMessage(ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Unknown parameter, " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " <version|reload|blocklist|toollist|toggle|pattern|mode>");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        List<String> values = new ArrayList<>();

        if (args.length == 1) {
            values.add("version");
            if (sender.hasPermission(VMConstants.PERMISSION_RELOAD)) {
                values.add("reload");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_TOGGLE)) {
                values.add("toggle");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_MODE)) {
                values.add("mode");
            }
            if (hasBlocklistPerms(sender)) {
                values.add("blocklist");
            }
            if (hasToolListPerms(sender)) {
                values.add("toollist");
            }
            if (sender.hasPermission(VMConstants.PERMISSION_PATTERN)) {
                values.add("pattern");
            }
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("blocklist") || args[0].equalsIgnoreCase("toollist")) {
                for (ToolCategory category : ToolCategory.getAll()) {
                    values.add(category.getId().toLowerCase());
                }

                if (args[0].equalsIgnoreCase("toollist")) {
                    values.remove("hand"); // Cannot modify the hand's tool list
                }
            }

            else if (args[0].equalsIgnoreCase("mode")) {
                for (ActivationStrategy activationStrategy : ActivationStrategy.values()) {
                    values.add(activationStrategy.name().toLowerCase());
                }
            }

            else if (args[0].equalsIgnoreCase("pattern")) {
                values = plugin.getPatternRegistry().getPatterns().stream()
                        .map(VeinMiningPattern::getKey).map(NamespacedKey::toString)
                        .collect(Collectors.toList());
            }
        }

        else if (args.length == 3) {
            String listType = args[0].toLowerCase();
            if (listType.equals("blocklist") || listType.equals("toollist")) {
                if (sender.hasPermission(String.format(VMConstants.PERMISSION_DYNAMIC_LIST_ADD, listType))) {
                    values.add("add");
                }
                if (sender.hasPermission(String.format(VMConstants.PERMISSION_DYNAMIC_LIST_REMOVE, listType))) {
                    values.add("remove");
                }
                if (sender.hasPermission(String.format(VMConstants.PERMISSION_DYNAMIC_LIST_LIST, listType) + ".*")) {
                    values.add("list");
                }
            }
        }

        else if (args.length >= 4) {
            List<String> suggestions = Collections.emptyList();

            // Prefix arguments with "minecraft:" if necessary
            String materialArg = args[args.length - 1];
            if (!materialArg.startsWith("minecraft:")) {
                materialArg = (materialArg.startsWith(":") ? "minecraft" : "minecraft:") + materialArg;
            }

            // Impossible suggestions (defaults to previous arguments)
            List<String> impossibleSuggestions = new ArrayList<>();
            for (int i = 3; i < args.length - 1; i++) {
                impossibleSuggestions.add(args[i]);
                impossibleSuggestions.add("minecraft:" + args[i]);
            }

            if (args[2].equalsIgnoreCase("add")) {
                if (args[0].equalsIgnoreCase("blocklist")) {
                    suggestions = StringUtil.copyPartialMatches(materialArg, BLOCK_KEYS, new ArrayList<>());

                    ToolCategory category = ToolCategory.get(args[1]);

                    if (category != null) {
                        BlockList blocklist = category.getBlocklist();

                        if (!blocklist.containsWildcard()) {
                            suggestions.add("*");
                        }

                        blocklist.forEach(b -> impossibleSuggestions.add(b.getType().getKey().toString()));
                    }
                }
                else if (args[0].equalsIgnoreCase("toollist")) {
                    suggestions = StringUtil.copyPartialMatches(materialArg, ITEM_KEYS, new ArrayList<>());
                }
            }

            else if (args[2].equalsIgnoreCase("remove")) {
                if (args[0].equalsIgnoreCase("blocklist")) {
                    ToolCategory category = ToolCategory.get(args[1]);
                    if (category != null) {
                        BlockList blocklist = category.getBlocklist();
                        List<String> blocklistSuggestions = new ArrayList<>(blocklist.size());
                        blocklist.forEach(v -> blocklistSuggestions.add(v.asDataString()));

                        suggestions = StringUtil.copyPartialMatches(materialArg, blocklistSuggestions, new ArrayList<>());
                    }
                }
                else if (args[0].equalsIgnoreCase("toollist")) {
                    suggestions = StringUtil.copyPartialMatches(materialArg, ITEM_KEYS, new ArrayList<>());
                }
            }

            suggestions.removeAll(impossibleSuggestions);
            return suggestions;
        }

        else {
            return Collections.emptyList();
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], values, new ArrayList<>());
    }

    private String formatBlockData(String blockData) {
        StringBuilder newBlockData = new StringBuilder(ChatColor.YELLOW.toString()).append(blockData);

        int blockDataBracketIndex = newBlockData.indexOf("[");
        if (blockDataBracketIndex == -1) {
            return newBlockData.toString();
        }

        // Colourize the states
        String blockStateString = newBlockData.substring(blockDataBracketIndex + 1, newBlockData.length() - 1);
        newBlockData.delete(blockDataBracketIndex, newBlockData.length());

        StringBuilder newBlockStateString = new StringBuilder(ChatColor.WHITE.toString()).append('[');

        String[] blockStates = blockStateString.split(",");
        for (int i = 0; i < blockStates.length; i++) {
            String state = blockStates[i];
            String[] stateValues = state.split("=");
            newBlockStateString.append(ChatColor.AQUA).append(stateValues[0]).append(ChatColor.WHITE).append('=').append(ChatColor.GOLD).append(stateValues[1]);

            if (i < blockStates.length - 1) {
                newBlockStateString.append(ChatColor.WHITE).append(',');
            }
        }

        // Inject the block states into colourized block data
        newBlockStateString.append(ChatColor.WHITE).append("]");
        return newBlockData.append(newBlockStateString.toString()).append(ChatColor.RESET).toString();
    }

    private boolean hasBlocklistPerms(CommandSender sender) {
        return sender.hasPermission(VMConstants.PERMISSION_BLOCKLIST_ADD)
            || sender.hasPermission(VMConstants.PERMISSION_BLOCKLIST_REMOVE)
            || sender.hasPermission(VMConstants.PERMISSION_BLOCKLIST_LIST + ".*");
    }

    private boolean hasToolListPerms(CommandSender sender) {
        return sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_ADD)
            || sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_REMOVE)
            || sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_LIST + ".*");
    }

    private boolean canVeinMine(Player player) {
        for (ToolCategory category : ToolCategory.getAll()) {
            if (category.hasPermission(player)) {
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
        return (result != null && result.requiresUpdate()) ? " (" + ChatColor.GREEN + ChatColor.BOLD + "UPDATE AVAILABLE!" + ChatColor.GRAY + ")" : "";
    }

}
