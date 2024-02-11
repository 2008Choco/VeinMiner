package wtf.choco.veinminer.config.impl;

import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.config.ConfigWrapper;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.pattern.VeinMiningPatternDefault;

import static wtf.choco.veinminer.config.impl.ConfigKeys.*;

public final class StandardVeinMinerConfiguration implements VeinMinerConfiguration {

    private final VeinMinerPlugin plugin;

    public StandardVeinMinerConfiguration(VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isMetricsEnabled() {
        return plugin.getConfig().getBoolean(KEY_METRICS_ENABLED, true);
    }

    @Override
    public boolean isPerformUpdateChecks() {
        return plugin.getConfig().getBoolean(KEY_PERFORM_UPDATE_CHECKS, true);
    }

    @NotNull
    @Override
    public ActivationStrategy getDefaultActivationStrategy() {
        ActivationStrategy strategy = ActivationStrategy.SNEAK;

        String strategyString = plugin.getConfig().getString(KEY_DEFAULT_ACTIVATION_STRATEGY);
        if (strategyString != null) {
            strategy = Enums.getIfPresent(ActivationStrategy.class, strategyString.toUpperCase()).or(strategy);
        }

        return strategy;
    }

    @NotNull
    @Override
    public VeinMiningPattern getDefaultVeinMiningPattern() {
        VeinMiningPattern pattern = VeinMiningPatternDefault.getInstance();

        String patternString = plugin.getConfig().getString(KEY_DEFAULT_VEIN_MINING_PATTERN);
        if (patternString != null) {
            pattern = plugin.getPatternRegistry().getOrDefault(patternString, pattern);
        }

        return pattern;
    }

    @Override
    public boolean isCollectItemsAtSource() {
        return plugin.getConfig().getBoolean(KEY_COLLECT_ITEMS_AT_SOURCE, true);
    }

    @Override
    public boolean isNerfMcMMO() {
        return plugin.getConfig().getBoolean(KEY_NERF_MCMMO, false);
    }

    @Override
    public boolean isRepairFriendly() {
        return plugin.getConfig().getBoolean(KEY_REPAIR_FRIENDLY, false);
    }

    @Override
    public int getMaxVeinSize() {
        return plugin.getConfig().getInt(KEY_MAX_VEIN_SIZE, 64);
    }

    @Override
    public double getCost() {
        return plugin.getConfig().getDouble(KEY_COST, 0.0D);
    }

    @Override
    public boolean isDisabledWorld(@NotNull String worldName) {
        return getDisabledWorlds().contains(worldName);
    }

    @NotNull
    @Unmodifiable
    @Override
    public Set<String> getDisabledWorlds() {
        return ImmutableSet.copyOf(plugin.getConfig().getStringList(KEY_DISABLED_WORLDS));
    }

    @Override
    public boolean isDisabledGameMode(@NotNull GameMode gameMode) {
        return getDisabledGameModes().contains(gameMode);
    }

    @NotNull
    @Unmodifiable
    @Override
    public Set<GameMode> getDisabledGameModes() {
        ImmutableSet.Builder<GameMode> disabledGameModes = ImmutableSet.builder();

        for (String gameModeName : plugin.getConfig().getStringList(KEY_DISABLED_GAME_MODES)) {
            GameMode gameMode = Enums.getIfPresent(GameMode.class, gameModeName.toUpperCase()).orNull();
            if (gameMode != null) {
                disabledGameModes.add(gameMode);
            }
        }

        return disabledGameModes.build();
    }

    @Override
    public float getHungerModifier() {
        return Math.max((float) plugin.getConfig().getDouble(KEY_HUNGER_HUNGER_MODIFIER, 4.0D), 0.0F);
    }

    @Override
    public int getMinimumFoodLevel() {
        return Math.max(plugin.getConfig().getInt(KEY_HUNGER_MINIMUM_FOOD_LEVEL, 1), 0);
    }

    @Nullable
    @Override
    public String getHungryMessage() {
        return plugin.getConfig().getString(KEY_HUNGER_HUNGRY_MESSAGE);
    }

    @NotNull
    @Override
    public ClientConfig getClientConfiguration() {
        FileConfiguration config = plugin.getConfig();

        return ClientConfig.builder()
                .allowActivationKeybind(config.getBoolean(KEY_CLIENT_ALLOW_ACTIVATION_KEYBIND, true))
                .allowPatternSwitchingKeybind(config.getBoolean(KEY_CLIENT_ALLOW_PATTERN_SWITCHING_KEYBIND, true))
                .allowWireframeRendering(config.getBoolean(KEY_CLIENT_ALLOW_WIREFRAME_RENDERING, true))
                .build();
    }

    @NotNull
    @Override
    public PersistentDataStorage.@NotNull Type getStorageType() {
        PersistentDataStorage.Type type = PersistentDataStorage.Type.SQLITE;

        String typeString = plugin.getConfig().getString(KEY_STORAGE_TYPE);
        if (typeString != null) {
            type = Enums.getIfPresent(PersistentDataStorage.Type.class, typeString.toUpperCase()).or(type);
        }

        return type;
    }

    @Nullable
    @Override
    public File getJsonStorageDirectory() {
        String directoryName = plugin.getConfig().getString(KEY_STORAGE_JSON_DIRECTORY);
        return directoryName != null ? new File(".", directoryName.replace("%plugin%", "plugins/" + plugin.getDataFolder().getName())) : null;
    }

    @Nullable
    @Override
    public String getMySQLHost() {
        return plugin.getConfig().getString(KEY_STORAGE_MYSQL_HOST);
    }

    @Override
    public int getMySQLPort() {
        return plugin.getConfig().getInt(KEY_STORAGE_MYSQL_PORT);
    }

    @Nullable
    @Override
    public String getMySQLUsername() {
        return plugin.getConfig().getString(KEY_STORAGE_MYSQL_USERNAME);
    }

    @Nullable
    @Override
    public String getMySQLPassword() {
        return plugin.getConfig().getString(KEY_STORAGE_MYSQL_PASSWORD);
    }

    @Nullable
    @Override
    public String getMySQLDatabase() {
        return plugin.getConfig().getString(KEY_STORAGE_MYSQL_DATABASE);
    }

    @Nullable
    @Override
    public String getMySQLTablePrefix() {
        return plugin.getConfig().getString(KEY_STORAGE_MYSQL_TABLE_PREFIX);
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getGlobalBlockListKeys() {
        return ImmutableList.copyOf(plugin.getConfig().getStringList(KEY_BLOCKLIST_ALL));
    }

    @Override
    public void setBlockListKeys(@NotNull String categoryId, @NotNull BlockList blockList) {
        List<String> blockListStrings = blockList.asList().stream()
                .map(VeinMinerBlock::toStateString)
                .sorted().toList();

        this.plugin.getConfig().set(keyBlockList(categoryId), blockListStrings);
        this.plugin.saveConfig();
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getBlockListKeys(@NotNull String categoryId) {
        return ImmutableList.copyOf(plugin.getConfig().getStringList(keyBlockList(categoryId)));
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getAliasStrings() {
        return ImmutableList.copyOf(plugin.getConfig().getStringList(KEY_ALIASES));
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getDefinedCategoryIds() {
        return plugin.getCategoriesConfig().asRawConfig().getKeys(false);
    }

    @Nullable
    @Override
    public ToolCategoryConfiguration getToolCategoryConfiguration(@NotNull String categoryId) {
        ConfigWrapper categoriesConfig = this.plugin.getCategoriesConfig();
        if (!categoriesConfig.asRawConfig().contains(categoryId)) {
            return null;
        }

        return new StandardToolCategoryConfiguration(categoryId, categoriesConfig, this);
    }

}