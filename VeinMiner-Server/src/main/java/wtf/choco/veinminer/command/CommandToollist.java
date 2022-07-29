package wtf.choco.veinminer.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.platform.PlatformCommandSender;
import wtf.choco.veinminer.platform.world.ItemType;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.tool.VeinMinerToolCategoryHand;
import wtf.choco.veinminer.util.ChatFormat;
import wtf.choco.veinminer.util.StringUtils;

public final class CommandToollist implements CommandExecutor {

    private static final List<String> ITEM_KEYS = VeinMinerServer.getInstance().getPlatform().getAllItemTypeKeys();
    private static final List<String> ARGUMENTS_1 = List.of("add", "remove", "list");

    private final VeinMinerServer veinMiner;

    public CommandToollist(@NotNull VeinMinerServer veinMiner) {
        this.veinMiner = veinMiner;
    }

    @Override
    public boolean execute(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            return false;
        }

        VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            sender.sendMessage(ChatFormat.RED + "Unknown tool category, " + args[0]);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                return false;
            }

            if (category instanceof VeinMinerToolCategoryHand) {
                sender.sendMessage(ChatFormat.RED + "Cannot add tools to the hand category.");
                return true;
            }

            String itemArg = args[2].toLowerCase();
            ItemType itemType = veinMiner.getPlatform().getItemType(itemArg);

            if (itemType == null) {
                sender.sendMessage(ChatFormat.RED + "Unknown item type. " + ChatFormat.GRAY + "Given " + ChatFormat.YELLOW + itemArg + ChatFormat.GRAY + ".");
                return true;
            }

            if (!category.addItem(itemType)) {
                sender.sendMessage(ChatFormat.RED + itemType.getKey().toString() + " is already on the " + category.getId() + " tool list.");
                return true;
            }

            // Update configuration
            this.veinMiner.getPlatform().getConfig().updateToolList(category);

            sender.sendMessage(ChatFormat.YELLOW + itemType.getKey().toString() + ChatFormat.GRAY + " successfully added to the tool list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                return false;
            }

            if (category instanceof VeinMinerToolCategoryHand) {
                sender.sendMessage(ChatFormat.RED + "Cannot remove tools from the hand category.");
                return true;
            }

            if (category.getItems().size() == 1) {
                sender.sendMessage(ChatFormat.RED + "The " + category.getId() + " category has only 1 item. Cannot remove from in game.");
                return true;
            }

            String itemArg = args[2].toLowerCase();
            ItemType itemType = veinMiner.getPlatform().getItemType(itemArg);

            if (itemType == null) {
                sender.sendMessage(ChatFormat.RED + "Unknown item type. " + ChatFormat.GRAY + "Given " + ChatFormat.YELLOW + itemArg + ChatFormat.GRAY + ".");
                return true;
            }

            if (!category.removeItem(itemType)) {
                sender.sendMessage(ChatFormat.RED + itemType.getKey().toString() + " is not on the " + category.getId() + " tool list.");
                return true;
            }

            // Update configuration
            this.veinMiner.getPlatform().getConfig().updateToolList(category);

            sender.sendMessage(ChatFormat.YELLOW + itemType.getKey().toString() + ChatFormat.GRAY + " successfully removed from the tool list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("list")) {
            Set<ItemType> items = category.getItems();

            if (items.isEmpty()) {
                sender.sendMessage(ChatFormat.RED + "The " + category.getId() + " category has no tools.");
                return true;
            }

            sender.sendMessage("");
            sender.sendMessage(ChatFormat.GREEN + "Tool list " + ChatFormat.GRAY + "for category " + ChatFormat.GREEN + category.getId().replace("_", " ") + ChatFormat.GRAY + ":");
            category.getItems().forEach(tool -> sender.sendMessage(ChatFormat.WHITE + " - " + ChatFormat.YELLOW + tool.getKey().toString()));
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
                if (categoryId.startsWith(args[0].toLowerCase()) && !(category instanceof VeinMinerToolCategoryHand)) {
                    suggestions.add(categoryId);
                }
            });

            return suggestions;
        }

        VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(args[0]);
        if (category == null || category instanceof VeinMinerToolCategoryHand) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return StringUtils.copyPartialMatches(args[1], ARGUMENTS_1, new ArrayList<>());
        }

        if (args.length == 3 && !args[1].equalsIgnoreCase("list")) {
            List<String> suggestions = new ArrayList<>();

            if (args[1].equalsIgnoreCase("add")) {
                ITEM_KEYS.forEach(itemKeyString -> {
                    if (itemKeyString.contains(args[2].toLowerCase())) {
                        suggestions.add(itemKeyString);
                    }
                });
            }

            else if (args[1].equalsIgnoreCase("remove")) {
                category.getItems().forEach(item -> {
                    String itemKeyString = item.getKey().toString();
                    if (itemKeyString.contains(args[2].toLowerCase())) {
                        suggestions.add(itemKeyString);
                    }
                });
            }

            return suggestions;
        }

        return Collections.emptyList();
    }

}
