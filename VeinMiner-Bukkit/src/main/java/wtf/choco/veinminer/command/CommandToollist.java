package wtf.choco.veinminer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.platform.BukkitItemType;
import wtf.choco.veinminer.platform.ItemType;
import wtf.choco.veinminer.tool.BukkitVeinMinerToolCategoryHand;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.ConfigWrapper;
import wtf.choco.veinminer.util.VMConstants;

public final class CommandToollist implements TabExecutor {

    private static final List<String> ITEM_KEYS = Arrays.stream(Material.values()).filter(Material::isItem).map(m -> m.getKey().toString()).toList();
    private static final List<String> ARGUMENTS_1 = List.of("add", "remove", "list");

    private final VeinMinerPlugin plugin;

    public CommandToollist(VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            return false;
        }

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            sender.sendMessage(ChatColor.RED + "Unknown tool category, " + args[0]);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_ADD)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 3) {
                return false;
            }

            if (category instanceof BukkitVeinMinerToolCategoryHand) {
                sender.sendMessage(ChatColor.RED + "Cannot add tools to the hand category.");
                return true;
            }

            String itemArg = args[2].toLowerCase();
            Material itemMaterial = Material.matchMaterial(args[2]);

            if (itemMaterial == null) {
                sender.sendMessage(ChatColor.RED + "Unknown item type. " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + itemArg + ChatColor.GRAY + ".");
                return true;
            }

            ItemType itemType = BukkitItemType.of(itemMaterial);

            if (!category.addItem(itemType)) {
                sender.sendMessage(ChatColor.RED + itemMaterial.getKey().toString() + " is already on the " + category.getId() + " tool list.");
                return true;
            }

            // Update configuration
            ConfigWrapper categoriesConfigWrapper = plugin.getCategoriesConfig();
            FileConfiguration categoriesConfig = categoriesConfigWrapper.asRawConfig();

            List<String> configItemList = categoriesConfig.getStringList(category.getId() + ".Items");
            configItemList.add(itemMaterial.getKey().toString());
            categoriesConfig.set(category.getId() + ".Items", configItemList);
            categoriesConfigWrapper.save();

            sender.sendMessage(ChatColor.YELLOW + itemMaterial.getKey().toString() + ChatColor.GRAY + " successfully added to the tool list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_REMOVE)) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            if (args.length < 3) {
                return false;
            }

            if (category instanceof BukkitVeinMinerToolCategoryHand) {
                sender.sendMessage(ChatColor.RED + "Cannot remove tools from the hand category.");
                return true;
            }

            if (category.getItems().size() == 1) {
                sender.sendMessage(ChatColor.RED + "The " + category.getId() + " category has only 1 item. Cannot remove from in game.");
                return true;
            }

            String itemArg = args[2].toLowerCase();
            Material itemMaterial = Material.matchMaterial(args[2]);

            if (itemMaterial == null) {
                sender.sendMessage(ChatColor.RED + "Unknown item type. " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + itemArg + ChatColor.GRAY + ".");
                return true;
            }

            ItemType itemType = BukkitItemType.of(itemMaterial);

            if (!category.removeItem(itemType)) {
                sender.sendMessage(ChatColor.RED + itemMaterial.getKey().toString() + " is not on the " + category.getId() + " tool list.");
                return true;
            }

            // Update configuration
            ConfigWrapper categoriesConfigWrapper = plugin.getCategoriesConfig();
            FileConfiguration categoriesConfig = categoriesConfigWrapper.asRawConfig();

            List<String> configItemList = categoriesConfig.getStringList(category.getId() + ".Items");
            configItemList.remove(itemMaterial.getKey().toString());
            categoriesConfig.set(category.getId() + ".Items", configItemList);
            categoriesConfigWrapper.save();

            sender.sendMessage(ChatColor.YELLOW + itemMaterial.getKey().toString() + ChatColor.GRAY + " successfully removed from the tool list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("list")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_TOOLLIST_LIST + "." + category.getId().toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command.");
                return true;
            }

            Set<ItemType> items = category.getItems();

            if (items.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "The " + category.getId() + " category has no tools.");
                return true;
            }

            sender.sendMessage("");
            sender.sendMessage(ChatColor.GREEN + "Tool list " + ChatColor.GRAY + "for category " + ChatColor.GREEN + category.getId().replace("_", " ") + ChatColor.GRAY + ":");
            category.getItems().forEach(tool -> sender.sendMessage(ChatColor.WHITE + " - " + ChatColor.YELLOW + tool.getKey().toString()));
            sender.sendMessage("");
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            this.plugin.getToolCategoryRegistry().getAll().forEach(category -> {
                String categoryId = category.getId().toLowerCase();
                if (categoryId.startsWith(args[0].toLowerCase()) && !(category instanceof BukkitVeinMinerToolCategoryHand)) {
                    suggestions.add(categoryId);
                }
            });

            return suggestions;
        }

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[0]);
        if (category == null || category instanceof BukkitVeinMinerToolCategoryHand) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], ARGUMENTS_1, new ArrayList<>());
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
