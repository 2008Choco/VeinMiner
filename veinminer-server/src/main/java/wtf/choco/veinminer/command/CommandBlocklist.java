package wtf.choco.veinminer.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.platform.PlatformCommandSender;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.ChatFormat;
import wtf.choco.veinminer.util.StringUtils;

public final class CommandBlocklist implements Command {

    private static final List<String> BLOCK_KEYS = VeinMinerServer.getInstance().getPlatform().getAllBlockTypeKeys();
    private static final List<String> ARGUMENTS_1 = List.of("add", "remove", "list");

    private final VeinMinerServer veinMiner;

    public CommandBlocklist(@NotNull VeinMinerServer veinMiner) {
        this.veinMiner = veinMiner;
    }

    @Override
    public boolean execute(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage("/" + label + " <category> <add|remove|list>");
            return true;
        }

        VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            sender.sendMessage(ChatFormat.RED + "Unknown tool category, " + args[0]);
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
            VeinMinerBlock block = VeinMinerBlock.fromString(blockArg);
            if (block == null) {
                sender.sendMessage(ChatFormat.RED + "Unknown block type/block state (was it an item)? " + ChatFormat.GRAY + "Given " + ChatFormat.YELLOW + blockArg + ChatFormat.GRAY + ".");
                return true;
            }

            if (!blockList.add(block)) {
                sender.sendMessage(formatBlockData(block.toStateString()) + ChatFormat.RED + " is already on the " + category.getId() + " block list.");
                return true;
            }

            // Update configuration
            this.veinMiner.getPlatform().getConfig().updateBlockList(category.getId(), blockList);

            sender.sendMessage(formatBlockData(block.toStateString()) + ChatFormat.GRAY + " successfully added to the block list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <block>");
                return true;
            }

            BlockList blockList = category.getBlockList();

            String blockArg = args[2].toLowerCase();
            VeinMinerBlock block = VeinMinerBlock.fromString(blockArg);
            if (block == null) {
                sender.sendMessage(ChatFormat.RED + "Unknown block type/block state (was it an item)? " + ChatFormat.GRAY + "Given " + ChatFormat.YELLOW + blockArg + ChatFormat.GRAY + ".");
                return true;
            }

            if (!blockList.remove(block)) {
                sender.sendMessage(formatBlockData(block.toStateString()) + ChatFormat.RED + " is not on the " + category.getId() + " block list.");
                return true;
            }

            // Update configuration
            this.veinMiner.getPlatform().getConfig().updateBlockList(category.getId(), blockList);

            sender.sendMessage(formatBlockData(block.toStateString()) + ChatFormat.GRAY + " successfully removed from the block list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("list")) {
            List<VeinMinerBlock> blockList = category.getBlockList().asList(null);

            if (blockList.isEmpty()) {
                sender.sendMessage(ChatFormat.RED + "The " + category.getId() + " category is empty.");
                return true;
            }

            sender.sendMessage("");
            sender.sendMessage(ChatFormat.GREEN + "Block list " + ChatFormat.GRAY + "for category " + ChatFormat.GREEN + category.getId().toLowerCase().replace("_", " ") + ChatFormat.GRAY + ":");
            blockList.forEach(block -> sender.sendMessage(ChatFormat.WHITE + " - " + formatBlockData(block.toStateString())));
            sender.sendMessage("");
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            this.veinMiner.getToolCategoryRegistry().getAll().forEach(category -> {
                String categoryId = category.getId().toLowerCase();
                if (categoryId.startsWith(args[0].toLowerCase())) {
                    suggestions.add(categoryId);
                }
            });

            return suggestions;
        }

        VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return StringUtils.copyPartialMatches(args[1], ARGUMENTS_1, new ArrayList<>());
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
        StringBuilder newBlockData = new StringBuilder(ChatFormat.YELLOW.toString()).append(blockData);

        int blockDataBracketIndex = newBlockData.indexOf("[");
        if (blockDataBracketIndex == -1) {
            return newBlockData.toString();
        }

        // Colourize the states
        String blockStateString = newBlockData.substring(blockDataBracketIndex + 1, newBlockData.length() - 1);
        newBlockData.delete(blockDataBracketIndex, newBlockData.length());

        StringBuilder newBlockStateString = new StringBuilder(ChatFormat.WHITE.toString()).append('[');

        String[] blockStates = blockStateString.split(",");
        for (int i = 0; i < blockStates.length; i++) {
            String state = blockStates[i];
            String[] stateValues = state.split("=");
            newBlockStateString.append(ChatFormat.AQUA).append(stateValues[0]).append(ChatFormat.WHITE).append('=').append(ChatFormat.GOLD).append(stateValues[1]);

            if (i < blockStates.length - 1) {
                newBlockStateString.append(ChatFormat.WHITE).append(',');
            }
        }

        // Inject the block states into colourized block data
        newBlockStateString.append(ChatFormat.WHITE).append("]");
        return newBlockData.append(newBlockStateString.toString()).append(ChatFormat.RESET).toString();
    }

}
