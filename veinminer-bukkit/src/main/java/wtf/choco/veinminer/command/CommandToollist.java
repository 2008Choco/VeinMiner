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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.tool.VeinMinerToolCategoryHand;
import wtf.choco.veinminer.util.StringUtils;

public final class CommandToollist implements TabExecutor {

    private static final List<String> ITEM_KEYS = Arrays.stream(Material.values()).filter(Material::isItem).map(material -> material.getKey().toString()).toList();
    private static final List<String> ARGUMENTS_1 = List.of("add", "remove", "list");

    private final VeinMinerPlugin plugin;

    public CommandToollist(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage("/" + label + " <category> <add|remove|list>");
            return true;
        }

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[0]);
        if (category == null) {
            sender.sendMessage(ChatColor.RED + "Unknown tool category, " + args[0]);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("/" + label + " " + args[0] + " <add|remove|list>");
            return true;
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <item>");
                return true;
            }

            if (category instanceof VeinMinerToolCategoryHand) {
                sender.sendMessage(ChatColor.RED + "Cannot add tools to the hand category.");
                return true;
            }

            String itemArg = args[2].toLowerCase();
            Material material = Material.matchMaterial(itemArg);

            if (material == null || !material.isItem()) {
                sender.sendMessage(ChatColor.RED + "Unknown item type. " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + itemArg + ChatColor.GRAY + ".");
                return true;
            }

            if (!category.addItem(material)) {
                sender.sendMessage(ChatColor.RED + material.getKey().toString() + " is already on the " + category.getId() + " tool list.");
                return true;
            }

            // Update configuration
            ToolCategoryConfiguration config = plugin.getConfiguration().getToolCategoryConfiguration(category.getId());
            if (config != null) {
                config.setItems(new ArrayList<>(category.getItems()));
            }

            sender.sendMessage(ChatColor.YELLOW + material.getKey().toString() + ChatColor.GRAY + " successfully added to the tool list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <item>");
                return true;
            }

            if (category instanceof VeinMinerToolCategoryHand) {
                sender.sendMessage(ChatColor.RED + "Cannot remove tools from the hand category.");
                return true;
            }

            if (category.getItems().size() == 1) {
                sender.sendMessage(ChatColor.RED + "The " + category.getId() + " category has only 1 item. Cannot remove from in game.");
                return true;
            }

            String itemArg = args[2].toLowerCase();
            Material material = Material.matchMaterial(itemArg);

            if (material == null || !material.isItem()) {
                sender.sendMessage(ChatColor.RED + "Unknown item type. " + ChatColor.GRAY + "Given " + ChatColor.YELLOW + itemArg + ChatColor.GRAY + ".");
                return true;
            }

            if (!category.removeItem(material)) {
                sender.sendMessage(ChatColor.RED + material.getKey().toString() + " is not on the " + category.getId() + " tool list.");
                return true;
            }

            // Update configuration
            ToolCategoryConfiguration config = plugin.getConfiguration().getToolCategoryConfiguration(category.getId());
            if (config != null) {
                config.setItems(new ArrayList<>(category.getItems()));
            }

            sender.sendMessage(ChatColor.YELLOW + material.getKey().toString() + ChatColor.GRAY + " successfully removed from the tool list.");
            return true;
        }

        else if (args[1].equalsIgnoreCase("list")) {
            Set<Material> items = category.getItems();

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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            this.plugin.getToolCategoryRegistry().getAll().forEach(category -> {
                String categoryId = category.getId().toLowerCase();
                if (categoryId.startsWith(args[0].toLowerCase()) && !(category instanceof VeinMinerToolCategoryHand)) {
                    suggestions.add(categoryId);
                }
            });

            return suggestions;
        }

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[0]);
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
