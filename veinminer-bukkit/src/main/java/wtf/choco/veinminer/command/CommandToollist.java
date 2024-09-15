package wtf.choco.veinminer.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.language.LanguageFile;
import wtf.choco.veinminer.language.LanguageKeys;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.tool.VeinMinerToolCategoryHand;

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
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <item>");
                return true;
            }

            if (category instanceof VeinMinerToolCategoryHand) {
                language.send(sender, LanguageKeys.COMMAND_TOOLLIST_ADD_HAND);
                return true;
            }

            String itemArg = args[2].toLowerCase();
            Material material = Material.matchMaterial(itemArg);

            if (material == null || !material.isItem()) {
                language.send(sender, LanguageKeys.COMMAND_UNKNOWN_ITEM, itemArg);
                return true;
            }

            if (!category.addItem(material)) {
                language.send(sender, LanguageKeys.COMMAND_TOOLLIST_ADD_EXISTS, material.getKey().toString(), category.getId());
                return true;
            }

            // Update configuration
            category.getConfiguration().setItems(new ArrayList<>(category.getItems()));

            language.send(sender, LanguageKeys.COMMAND_TOOLLIST_ADD_SUCCESS, material.getKey().toString(), category.getId());
            return true;
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                sender.sendMessage("/" + label + " " + args[0] + " " + args[1] + " <item>");
                return true;
            }

            if (category instanceof VeinMinerToolCategoryHand) {
                language.send(sender, LanguageKeys.COMMAND_TOOLLIST_REMOVE_HAND);
                return true;
            }

            if (category.getItems().size() == 1) {
                language.send(sender, LanguageKeys.COMMAND_TOOLLIST_REMOVE_TOO_FEW_ITEMS, category.getId());
                return true;
            }

            String itemArg = args[2].toLowerCase();
            Material material = Material.matchMaterial(itemArg);

            if (material == null || !material.isItem()) {
                language.send(sender, LanguageKeys.COMMAND_UNKNOWN_ITEM, itemArg);
                return true;
            }

            if (!category.removeItem(material)) {
                language.send(sender, LanguageKeys.COMMAND_TOOLLIST_REMOVE_MISSING, material.getKey().toString(), category.getId());
                return true;
            }

            // Update configuration
            category.getConfiguration().setItems(new ArrayList<>(category.getItems()));

            language.send(sender, LanguageKeys.COMMAND_TOOLLIST_REMOVE_SUCCESS, material.getKey().toString(), category.getId());
            return true;
        }

        else if (args[1].equalsIgnoreCase("list")) {
            Set<Material> items = category.getItems();

            if (items.isEmpty()) {
                language.send(sender, LanguageKeys.COMMAND_TOOLLIST_LIST_EMPTY, category.getId());
                return true;
            }

            sender.sendMessage("");
            language.send(sender, LanguageKeys.COMMAND_TOOLLIST_LIST_HEADER, category.getId());
            category.getItems().forEach(tool -> language.send(sender, LanguageKeys.COMMAND_TOOLLIST_LIST_ENTRY, tool.getKey().toString()));
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
