package wtf.choco.veinminer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.language.LanguageFile;
import wtf.choco.veinminer.language.LanguageKeys;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

public final class CommandBlocklist implements TabExecutor {

    private static final List<String> BLOCK_KEYS = Arrays.stream(Material.values()).filter(Material::isBlock).map(material -> material.getKey().toString()).toList();
    private static final List<String> ARGUMENTS_1 = List.of("add", "remove", "list");

    private final VeinMinerPlugin plugin;

    public CommandBlocklist(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage("/" + label + " <category> <add|remove|list>");
            return true;
        }

        LanguageFile language = plugin.getLanguage();

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            language.send(sender, LanguageKeys.COMMAND_UNKNOWN_CATEGORY, args[0]);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("/" + label + " " + args[0] + " <add|remove|list>");
            return true;
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <block>");
                return true;
            }

            BlockList blockList = category.getBlockList();

            String blockArg = args[2].toLowerCase();
            VeinMinerBlock block;
            try {
                block = VeinMinerBlock.fromString(blockArg);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
                return true;
            }

            if (!blockList.add(block)) {
                language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_ADD_EXISTS, formatBlockData(block.toStateString()), category.getId());
                return true;
            }

            // Update configuration
            category.getConfiguration().setBlockListKeys(blockList);

            language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_ADD_SUCCESS, formatBlockData(block.toStateString()), category.getId());
            return true;
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <block>");
                return true;
            }

            BlockList blockList = category.getBlockList();

            String blockArg = args[2].toLowerCase();
            VeinMinerBlock block;
            try {
                block = VeinMinerBlock.fromString(blockArg);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
                return true;
            }

            if (!blockList.remove(block)) {
                language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_REMOVE_MISSING, formatBlockData(block.toStateString()), category.getId());
                return true;
            }

            // Update configuration
            category.getConfiguration().setBlockListKeys(blockList);

            language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_REMOVE_SUCCESS, formatBlockData(block.toStateString()), category.getId());
            return true;
        }

        else if (args[1].equalsIgnoreCase("list")) {
            List<VeinMinerBlock> blockList = category.getBlockList().asList(null);

            if (blockList.isEmpty()) {
                language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_LIST_EMPTY, category.getId());
                return true;
            }

            sender.sendMessage("");
            language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_LIST_HEADER, category.getId());
            blockList.forEach(block -> language.send(sender, LanguageKeys.COMMAND_BLOCKLIST_LIST_ENTRY, formatBlockData(block.toStateString())));
            sender.sendMessage("");
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            this.plugin.getToolCategoryRegistry().getAll().forEach(category -> {
                String categoryId = category.getId().toLowerCase();
                if (categoryId.startsWith(args[0].toLowerCase())) {
                    suggestions.add(categoryId);
                }
            });

            return suggestions;
        }

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], ARGUMENTS_1, new ArrayList<>());
        }

        if (args.length == 3 && !args[1].equalsIgnoreCase("list")) {
            List<String> suggestions = new ArrayList<>();

            if (args[1].equalsIgnoreCase("add")) {
                BLOCK_KEYS.forEach(blockKeyString -> {
                    if (blockKeyString.contains(args[2].toLowerCase())) {
                        suggestions.add(blockKeyString);
                    }
                });
            }

            else if (args[1].equalsIgnoreCase("remove")) {
                category.getBlockList().asList().forEach(block -> {
                    String stateString = block.toStateString();
                    if (stateString.contains(args[2].toLowerCase())) {
                        suggestions.add(stateString);
                    }
                });
            }

            return suggestions;
        }

        return Collections.emptyList();
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

}
