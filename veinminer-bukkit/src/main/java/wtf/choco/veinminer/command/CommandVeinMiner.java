package wtf.choco.veinminer.command;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.data.LegacyImportTask;
import wtf.choco.veinminer.data.LegacyImportable;
import wtf.choco.veinminer.language.LanguageFile;
import wtf.choco.veinminer.language.LanguageKeys;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.update.UpdateResult;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

public final class CommandVeinMiner implements TabExecutor {

    private static final long IMPORT_CONFIRMATION_TIME_MILLIS = TimeUnit.SECONDS.toMillis(20);
    private static final List<String> NUMBERS = IntStream.range(1, 10).mapToObj(String::valueOf).toList();
    private static final List<String> SUGGESTION_OPTIONAL_AMOUNT = List.of("[amount]");

    private final Map<CommandSender, Long> requiresConfirmation = new HashMap<>();

    private final VeinMinerPlugin plugin;
    private final TabExecutor commandBlocklist;
    private final TabExecutor commandToollist;

    public CommandVeinMiner(@NotNull VeinMinerPlugin plugin, @NotNull TabExecutor commandBlocklist, @NotNull TabExecutor commandToollist) {
        this.plugin = plugin;
        this.commandBlocklist = commandBlocklist;
        this.commandToollist = commandToollist;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }

        LanguageFile language = plugin.getLanguage();

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_RELOAD)) {
                sender.sendMessage(language.get(LanguageKeys.COMMAND_INSUFFICIENT_PERMISSIONS));
                return true;
            }

            this.plugin.reloadConfig();
            this.plugin.getCategoriesConfig().reload();
            this.plugin.getVeinMinerManager().reloadFromConfig();
            this.plugin.getToolCategoryRegistry().reloadFromConfig();
            this.plugin.getLanguage().reload(plugin.getLogger());

            // Update configurations for all players
            this.plugin.getPlayerManager().getAll().forEach(veinMinerPlayer -> {
                veinMinerPlayer.setClientConfig(plugin.getConfiguration().getClientConfiguration(veinMinerPlayer.getPlayer()));
            });

            language.send(sender, LanguageKeys.COMMAND_VEINMINER_RELOAD_SUCCESS);
            return true;
        }

        else if (args[0].equalsIgnoreCase("version")) {
            PluginDescriptionFile description = plugin.getDescription();
            String headerFooter = language.get(LanguageKeys.COMMAND_VEINMINER_VERSION_BORDER);

            sender.sendMessage(headerFooter);
            sender.sendMessage("");
            sender.sendMessage(getVersionLine(language));
            language.send(sender, LanguageKeys.COMMAND_VEINMINER_VERSION_DEVELOPER, description.getAuthors().get(0));
            language.send(sender, LanguageKeys.COMMAND_VEINMINER_VERSION_WEBSITE, description.getWebsite());
            language.send(sender, LanguageKeys.COMMAND_VEINMINER_VERSION_WEBSITE, "https://github.com/2008Choco/VeinMiner");
            sender.sendMessage("");
            sender.sendMessage(headerFooter);
            return true;
        }

        else if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player player)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_TOGGLE_CONSOLE);
                return true;
            }

            if (!canVeinMine(player) || !player.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE)) {
                language.send(sender, LanguageKeys.COMMAND_INSUFFICIENT_PERMISSIONS);
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            // Toggle a specific tool
            if (args.length >= 2) {
                VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
                if (category == null) {
                    language.send(sender, LanguageKeys.COMMAND_UNKNOWN_CATEGORY, args[1]);
                    return true;
                }

                veinMinerPlayer.setVeinMinerEnabled(category, !veinMinerPlayer.isVeinMinerEnabled(category));
                if (veinMinerPlayer.isVeinMinerEnabled(category)) {
                    language.send(sender, LanguageKeys.COMMAND_VEINMINER_TOGGLE_SUCCESS_CATEGORY_ON, category.getId());
                } else {
                    language.send(sender, LanguageKeys.COMMAND_VEINMINER_TOGGLE_SUCCESS_CATEGORY_OFF, category.getId());
                }
            } else {
                // Toggle all tools
                veinMinerPlayer.setVeinMinerEnabled(!veinMinerPlayer.isVeinMinerEnabled());
                if (veinMinerPlayer.isVeinMinerEnabled()) {
                    language.send(sender, LanguageKeys.COMMAND_VEINMINER_TOGGLE_SUCCESS_ALL_ON);
                } else {
                    language.send(sender, LanguageKeys.COMMAND_VEINMINER_TOGGLE_SUCCESS_ALL_OFF);
                }
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("mode")) {
            if (!(sender instanceof Player player)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_MODE_CONSOLE);
                return true;
            }

            if (!canVeinMine(player) || !player.hasPermission(VMConstants.PERMISSION_COMMAND_MODE)) {
                language.send(sender, LanguageKeys.COMMAND_INSUFFICIENT_PERMISSIONS);
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("/" + label + " mode <" + Stream.of(ActivationStrategy.values()).map(strategy -> strategy.name().toLowerCase()).collect(Collectors.joining("|")) + ">");
                return true;
            }

            Optional<ActivationStrategy> strategyOptional = Enums.getIfPresent(ActivationStrategy.class, args[1].toUpperCase());
            if (!strategyOptional.isPresent()) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_MODE_INVALID, args[1]);
                return true;
            }

            ActivationStrategy strategy = strategyOptional.get();
            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            if (strategy == ActivationStrategy.CLIENT && !veinMinerPlayer.isUsingClientMod()) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_MODE_NO_CLIENT_MOD);

                // Let them know where to install VeinMiner on the client (if it's allowed)
                if (veinMinerPlayer.getClientConfig().isAllowActivationKeybind()) {
                    language.send(sender, LanguageKeys.COMMAND_VEINMINER_MODE_CLIENT_MOD_INFO);
                    player.sendMessage("https://www.curseforge.com/minecraft/mc-mods/veinminer-companion");
                    language.send(sender, LanguageKeys.COMMAND_VEINMINER_MODE_CLIENT_MOD_SUPPORTS);
                }

                return true;
            }

            veinMinerPlayer.setActivationStrategy(strategy);
            language.send(sender, LanguageKeys.COMMAND_VEINMINER_MODE_SUCCESS, strategy.getFriendlyName().toLowerCase());
            return true;
        }

        else if (args[0].equalsIgnoreCase("blocklist") && sender.hasPermission(VMConstants.PERMISSION_COMMAND_BLOCKLIST)) {
            this.commandBlocklist.onCommand(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("toollist") && sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOOLLIST)) {
            this.commandToollist.onCommand(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        else if (args[0].equalsIgnoreCase("pattern")) {
            if (!(sender instanceof Player player)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_PATTERN_CONSOLE);
                return true;
            }

            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN)) {
                language.send(sender, LanguageKeys.COMMAND_INSUFFICIENT_PERMISSIONS);
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("/" + label + " pattern <pattern>");
                return true;
            }

            NamespacedKey patternKey = NamespacedKey.fromString(args[1], plugin);
            if (patternKey == null) {
                language.send(sender, LanguageKeys.COMMAND_INVALID_KEY, args[1]);
                return true;
            }

            VeinMiningPattern pattern = plugin.getPatternRegistry().get(patternKey);
            if (pattern == null) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_PATTERN_UNKNOWN_PATTERN, patternKey);
                return true;
            }

            String permission = pattern.getPermission();
            if (permission != null && !player.hasPermission(permission)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_PATTERN_NO_PERMISSION);
                return true;
            }

            VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
            if (veinMinerPlayer == null) {
                return true;
            }

            PlayerVeinMiningPatternChangeEvent event = VMEventFactory.callPlayerVeinMiningPatternChangeEvent(player, veinMinerPlayer.getVeinMiningPattern(), pattern, PlayerVeinMiningPatternChangeEvent.Cause.COMMAND);
            if (event.isCancelled()) {
                return true;
            }

            pattern = event.getNewPattern();
            veinMinerPlayer.setVeinMiningPattern(pattern);

            language.send(sender, LanguageKeys.COMMAND_VEINMINER_PATTERN_SUCCESS, pattern.getKey());
            return true;
        }

        else if (args[0].equalsIgnoreCase("import")) {
            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_IMPORT)) {
                language.send(sender, LanguageKeys.COMMAND_INSUFFICIENT_PERMISSIONS);
                return true;
            }

            if (!(plugin.getPersistentDataStorage() instanceof LegacyImportable importable)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_NON_IMPORTABLE);
                return true;
            }

            if (System.currentTimeMillis() - requiresConfirmation.getOrDefault(sender, 0L) > IMPORT_CONFIRMATION_TIME_MILLIS) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_WARNING);
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_DESTRUCTIVE);
                sender.sendMessage("");
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_DESCRIPTION);
                sender.sendMessage("");
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_DO_ONCE);
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_CONFIRM);

                this.requiresConfirmation.put(sender, System.currentTimeMillis());
                return true;
            }

            this.requiresConfirmation.remove(sender);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new LegacyImportTask(plugin, sender, importable, plugin.getPersistentDataStorage().getType().getName()));
            return true;
        }

        else if (args[0].equalsIgnoreCase("givetool")) {
            if (!(sender instanceof Player player)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_GIVETOOL_CONSOLE);
                return true;
            }

            if (!sender.hasPermission(VMConstants.PERMISSION_COMMAND_GIVETOOL)) {
                language.send(sender, LanguageKeys.COMMAND_INSUFFICIENT_PERMISSIONS);
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("/" + label + " givetool <category> <item> [amount]");
                return true;
            }

            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
            if (category == null) {
                language.send(sender, LanguageKeys.COMMAND_UNKNOWN_CATEGORY, args[1]);
                return true;
            }

            if (category.getItems().isEmpty()) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_GIVETOOL_NO_ITEMS, category.getId().toLowerCase());
                return true;
            }

            Material material = Material.matchMaterial(args[2]);
            if (material == null || !material.isItem()) {
                language.send(sender, LanguageKeys.COMMAND_UNKNOWN_ITEM, args[2]);
                return true;
            } else if (!category.getItems().contains(material)) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_GIVETOOL_UNSUPPORTED_ITEM, args[2], category.getId());
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                amount = Math.max(1, parseInt(args[3], 1));
            }

            ItemStack itemStack = category.createItemStack(material, amount);
            if (!player.getInventory().addItem(itemStack).isEmpty()) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_GIVETOOL_INVENTORY_FULL);
            } else {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_GIVETOOL_SUCCESS, category.getId(), itemStack.getType().getKey());
            }

            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            suggestions.add("version");
            this.addConditionally(suggestions, "reload", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_RELOAD));
            this.addConditionally(suggestions, "blocklist", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_BLOCKLIST));
            this.addConditionally(suggestions, "toollist", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOOLLIST));
            this.addConditionally(suggestions, "givetool", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_GIVETOOL));
            this.addConditionally(suggestions, "toggle", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_TOGGLE));
            this.addConditionally(suggestions, "mode", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_MODE));
            this.addConditionally(suggestions, "pattern", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_PATTERN));
            this.addConditionally(suggestions, "import", () -> sender.hasPermission(VMConstants.PERMISSION_COMMAND_IMPORT));

            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        if (args[0].equalsIgnoreCase("blocklist")) {
            return commandBlocklist.onTabComplete(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args[0].equalsIgnoreCase("toollist")) {
            return commandToollist.onTabComplete(sender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();

            if (args[0].equalsIgnoreCase("toggle")) {
                this.plugin.getToolCategoryRegistry().getAll().forEach(category -> suggestions.add(category.getId().toLowerCase()));
            }

            else if (args[0].equalsIgnoreCase("givetool")) {
                this.plugin.getToolCategoryRegistry().getAll().forEach(category -> {
                    if (!category.getItems().isEmpty()) {
                        suggestions.add(category.getId().toLowerCase());
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("mode") && sender instanceof Player player) {
                VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
                if (veinMinerPlayer == null) {
                    return Collections.emptyList();
                }

                for (ActivationStrategy activationStrategy : ActivationStrategy.values()) {
                    if (activationStrategy == ActivationStrategy.CLIENT && !veinMinerPlayer.getClientConfig().isAllowActivationKeybind()) {
                        continue;
                    }

                    suggestions.add(activationStrategy.name().toLowerCase());
                }
            }

            else if (args[0].equalsIgnoreCase("pattern")) {
                for (VeinMiningPattern pattern : plugin.getPatternRegistry().getPatterns()) {
                    String permission = pattern.getPermission();
                    if (permission != null && !sender.hasPermission(permission)) {
                        continue;
                    }

                    String patternKey = pattern.getKey().toString();
                    if (patternKey.contains(args[1])) {
                        suggestions.add(patternKey);
                    }
                }
            }

            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }

        else if (args[0].equalsIgnoreCase("givetool")) {
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(args[1]);
            if (category == null) {
                return Collections.emptyList();
            }

            if (args.length == 3) {
                List<String> suggestions = new ArrayList<>();
                category.getItems().forEach(material -> suggestions.add(material.getKey().toString()));
                return StringUtil.copyPartialMatches(args[2], suggestions, new ArrayList<>());
            }

            else if (args.length == 4) {
                if (args[3].isEmpty()) {
                    return SUGGESTION_OPTIONAL_AMOUNT;
                }

                if (isNumber(args[3])) {
                    return Lists.transform(NUMBERS, value -> args[3] + value);
                } else {
                    return NUMBERS;
                }
            }
        }

        return Collections.emptyList();
    }

    private boolean isNumber(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private int parseInt(String input, int defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private <T> void addConditionally(Collection<T> collection, T value, BooleanSupplier predicate) {
        if (predicate.getAsBoolean()) {
            collection.add(value);
        }
    }

    private boolean canVeinMine(Player player) {
        for (VeinMinerToolCategory category : plugin.getToolCategoryRegistry().getAll()) {
            if (player.hasPermission(VMConstants.PERMISSION_VEINMINE.apply(category))) {
                return true;
            }
        }

        return false;
    }

    private String getVersionLine(LanguageFile language) {
        UpdateResult result = plugin.getUpdateChecker().getLastUpdateResult().orElse(null);
        if (result == null) {
            return language.get(LanguageKeys.COMMAND_VEINMINER_VERSION_VERSION);
        }

        return language.get(LanguageKeys.COMMAND_VEINMINER_VERSION_VERSION_ALERT, getUpdateSuffix(language, result));
    }

    private String getUpdateSuffix(LanguageFile language, UpdateResult result) {
        if (result.isUpdateAvailable()) {
            return language.get(LanguageKeys.COMMAND_VEINMINER_VERSION_VERSION_UPDATE_AVAILABLE);
        } else if (result.isUnreleased()) {
            return language.get(LanguageKeys.COMMAND_VEINMINER_VERSION_VERSION_DEV_BUILD);
        } else if (result.isFailed()) {
            return language.get(LanguageKeys.COMMAND_VEINMINER_VERSION_VERSION_FAILED);
        } else {
            return ChatColor.DARK_RED + "<unhandled>";
        }
    }

}
