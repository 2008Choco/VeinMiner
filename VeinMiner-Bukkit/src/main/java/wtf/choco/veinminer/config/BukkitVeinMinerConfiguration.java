package wtf.choco.veinminer.config;

import com.google.common.base.Enums;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.VMConstants;

/**
 * Bukkit implementation of {@link VeinMinerConfiguration}.
 */
public final class BukkitVeinMinerConfiguration implements VeinMinerConfiguration {

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link BukkitVeinMinerConfiguration}.
     *
     * @param plugin the plugin instance
     */
    public BukkitVeinMinerConfiguration(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean shouldCheckForUpdates() {
        return plugin.getConfig().getBoolean(VMConstants.CONFIG_PERFORM_UPDATE_CHECKS, true);
    }

    @NotNull
    @Override
    public List<String> getGlobalBlockList() {
        return plugin.getConfig().getStringList("BlockList.Global");
    }

    @NotNull
    @Override
    public List<String> getCategoryBlockList(@NotNull String categoryId) {
        return plugin.getConfig().getStringList("BlockList." + categoryId);
    }

    @NotNull
    @Override
    public ActivationStrategy getDefaultActivationStrategy() {
        String defaultActivationStrategyString = plugin.getConfig().getString(VMConstants.CONFIG_DEFAULT_ACTIVATION_STRATEGY);
        if (defaultActivationStrategyString == null) {
            return ActivationStrategy.SNEAK;
        }

        return Enums.getIfPresent(ActivationStrategy.class, defaultActivationStrategyString.toUpperCase()).or(ActivationStrategy.SNEAK);
    }

    @NotNull
    @Override
    public VeinMiningPattern getDefaultVeinMiningPattern() {
        VeinMinerServer veinMiner = VeinMinerServer.getInstance();

        String defaultVeinMiningPatternString = plugin.getConfig().getString(VMConstants.CONFIG_DEFAULT_VEIN_MINING_PATTERN);
        if (defaultVeinMiningPatternString == null) {
            return veinMiner.getDefaultVeinMiningPattern();
        }

        return veinMiner.getPatternRegistry().getOrDefault(defaultVeinMiningPatternString, veinMiner.getDefaultVeinMiningPattern());
    }

    @Override
    public boolean shouldCollectItemsAtSource() {
        return plugin.getConfig().getBoolean(VMConstants.CONFIG_COLLECT_ITEMS_AT_SOURCE, true);
    }

    @Override
    public boolean isRepairFriendly() {
        return plugin.getConfig().getBoolean(VMConstants.CONFIG_REPAIR_FRIENDLY, false);
    }

    @Override
    public boolean isRepairFriendly(@NotNull String categoryId) {
        return isRepairFriendly(categoryId, false);
    }

    @Override
    public boolean isRepairFriendly(@NotNull String categoryId, boolean defaultValue) {
        return plugin.getCategoriesConfig().asRawConfig().getBoolean(categoryId + "." + VMConstants.CONFIG_REPAIR_FRIENDLY, defaultValue);
    }

    @Override
    public int getMaxVeinSize() {
        return plugin.getConfig().getInt(VMConstants.CONFIG_MAX_VEIN_SIZE, 64);
    }

    @Override
    public int getMaxVeinSize(@NotNull String categoryId) {
        return getMaxVeinSize(categoryId, 64);
    }

    @Override
    public int getMaxVeinSize(@NotNull String categoryId, int defaultValue) {
        return plugin.getCategoriesConfig().asRawConfig().getInt(categoryId + "." + VMConstants.CONFIG_MAX_VEIN_SIZE, defaultValue);
    }

    @Override
    public double getCost() {
        return plugin.getConfig().getDouble(VMConstants.CONFIG_COST, 0.0);
    }

    @Override
    public double getCost(@NotNull String categoryId) {
        return getCost(categoryId, 0.0);
    }

    @Override
    public double getCost(@NotNull String categoryId, double defaultValue) {
        return plugin.getCategoriesConfig().asRawConfig().getDouble(categoryId + "." + VMConstants.CONFIG_COST, defaultValue);
    }

    @Override
    public int getPriority(@NotNull String categoryId) {
        return plugin.getCategoriesConfig().asRawConfig().getInt(categoryId + "." + VMConstants.CONFIG_PRIORITY, 0);
    }

    @Nullable
    @Override
    public String getNBTValue(@NotNull String categoryId) {
        return plugin.getCategoriesConfig().asRawConfig().getString(categoryId + "." + VMConstants.CONFIG_NBT);
    }

    @NotNull
    @Override
    public List<String> getDisabledGameModeNames() {
        return plugin.getConfig().getStringList(VMConstants.CONFIG_DISABLED_GAME_MODES);
    }

    @NotNull
    @Override
    public Set<String> getDisabledWorlds() {
        return new HashSet<>(plugin.getConfig().getStringList(VMConstants.CONFIG_DISABLED_WORLDS));
    }

    @NotNull
    @Override
    public Set<String> getDisabledWorlds(@NotNull String categoryId) {
        return new HashSet<>(plugin.getCategoriesConfig().asRawConfig().getStringList(categoryId + "." + VMConstants.CONFIG_DISABLED_WORLDS));
    }

    @NotNull
    @Override
    public Set<String> getDisabledWorlds(@NotNull String categoryId, Supplier<Set<String>> defaultValues) {
        String key = categoryId + "." + VMConstants.CONFIG_DISABLED_WORLDS;
        FileConfiguration config = plugin.getCategoriesConfig().asRawConfig();

        return config.contains(key, true) ? new HashSet<>(config.getStringList(key)) : defaultValues.get();
    }

    @Override
    public float getHungerModifier() {
        return (float) plugin.getConfig().getDouble(VMConstants.CONFIG_HUNGER_HUNGER_MODIFIER, 4.0);
    }

    @Override
    public int getMinimumFoodLevel() {
        return plugin.getConfig().getInt(VMConstants.CONFIG_HUNGER_MINIMUM_FOOD_LEVEL, 1);
    }

    @Nullable
    @Override
    public String getHungryMessage() {
        return plugin.getConfig().getString(VMConstants.CONFIG_HUNGER_HUNGRY_MESSAGE);
    }

    @NotNull
    @Override
    public ClientConfig getClientConfiguration() {
        FileConfiguration config = plugin.getConfig();

        return ClientConfig.builder()
                .allowActivationKeybind(config.getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_ACTIVATION_KEYBIND, true))
                .allowPatternSwitchingKeybind(config.getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_PATTERN_SWITCHING_KEYBIND, true))
                .allowWireframeRendering(config.getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_WIREFRAME_RENDERING, true))
                .build();
    }

    @NotNull
    @Override
    public PersistentDataStorage.@NotNull Type getStorageType() {
        String type = plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_TYPE);
        if (type == null) {
            return PersistentDataStorage.Type.SQLITE;
        }

        return Enums.getIfPresent(PersistentDataStorage.Type.class, type.toUpperCase()).or(PersistentDataStorage.Type.SQLITE);
    }

    @Nullable
    @Override
    public File getJsonStorageDirectory() {
        String directoryName = plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_JSON_DIRECTORY);
        return directoryName != null ? new File(".", directoryName.replace("%plugin%", "plugins/" + plugin.getDataFolder().getName())) : null;
    }

    @Nullable
    @Override
    public String getMySQLHost() {
        return plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_MYSQL_HOST);
    }

    @Override
    public int getMySQLPort() {
        return plugin.getConfig().getInt(VMConstants.CONFIG_STORAGE_MYSQL_PORT);
    }

    @Nullable
    @Override
    public String getMySQLUsername() {
        return plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_MYSQL_USERNAME);
    }

    @Nullable
    @Override
    public String getMySQLPassword() {
        return plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_MYSQL_PASSWORD);
    }

    @Nullable
    @Override
    public String getMySQLDatabase() {
        return plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_MYSQL_DATABASE);
    }

    @Nullable
    @Override
    public String getMySQLTablePrefix() {
        return plugin.getConfig().getString(VMConstants.CONFIG_STORAGE_MYSQL_TABLE_PREFIX);
    }

    @NotNull
    @Override
    public List<String> getRawAliasStrings() {
        return plugin.getConfig().getStringList(VMConstants.CONFIG_ALIASES);
    }

    @NotNull
    @Override
    public Set<String> getAllDefinedCategoryIds() {
        return plugin.getCategoriesConfig().asRawConfig().getKeys(false);
    }

    @NotNull
    @Override
    public List<String> getCategoryItemList(@NotNull String categoryId) {
        return plugin.getCategoriesConfig().asRawConfig().getStringList(categoryId + ".Items");
    }

    @Override
    public void updateBlockList(@NotNull String categoryId, @NotNull BlockList blockList) {
        List<String> blockListStrings = blockList.asList().stream()
                .map(block -> block.getType().getKey().toString())
                .sorted().toList();

        this.plugin.getConfig().set("BlockList." + categoryId, blockListStrings);
        this.plugin.saveConfig();
    }

    @Override
    public void updateToolList(@NotNull VeinMinerToolCategory category) {
        List<String> itemListStrings = category.getItems().stream()
                .map(item -> item.getKey().toString())
                .sorted().toList();

        this.plugin.getCategoriesConfig().asRawConfig().set(category.getId() + ".Items", itemListStrings);
        this.plugin.getCategoriesConfig().save();
    }

    @Override
    public void saveDefaults() {
        this.plugin.saveDefaultConfig();
        // The default categoriesConfig is already saved when instantiated
    }

    @Override
    public void reload() {
        this.plugin.reloadConfig();
        this.plugin.getCategoriesConfig().reload();
    }

}
