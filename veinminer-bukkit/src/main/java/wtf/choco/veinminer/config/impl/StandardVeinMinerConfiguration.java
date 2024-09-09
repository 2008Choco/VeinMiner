package wtf.choco.veinminer.config.impl;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.config.ConfigWrapper;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.data.PersistentStorageType;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.pattern.VeinMiningPatternDefault;
import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.util.VMConstants;

import static wtf.choco.veinminer.config.impl.ConfigKeys.*;

/**
 * A standard {@link VeinMinerConfiguration} implementation.
 */
public final class StandardVeinMinerConfiguration implements VeinMinerConfiguration {

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link StandardVeinMinerConfiguration}.
     *
     * @param plugin the plugin instance
     */
    public StandardVeinMinerConfiguration(@NotNull VeinMinerPlugin plugin) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
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
    public boolean isCollectExperienceAtSource() {
        return plugin.getConfig().getBoolean(KEY_COLLECT_EXPERIENCE_AT_SOURCE, isCollectItemsAtSource());
    }

    @Override
    public boolean isOnlyDamageOnFirstBlock() {
        return plugin.getConfig().getBoolean(KEY_ONLY_DAMAGE_ON_FIRST_BLOCK, true);
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
        return createClientConfiguration(null);
    }

    @NotNull
    @Override
    public ClientConfig getClientConfiguration(@NotNull Permissible permissible) {
        return createClientConfiguration(permissible);
    }

    @NotNull
    private ClientConfig createClientConfiguration(@Nullable Permissible permissible) {
        return ClientConfig.builder()
                .allowActivationKeybind(isAllowActivationKeybind() && test(permissible, VMConstants.PERMISSION_CLIENT_ACTIVATION))
                .allowPatternSwitchingKeybind(isAllowPatternSwitchingKeybind() && test(permissible, VMConstants.PERMISSION_CLIENT_PATTERNS))
                .allowWireframeRendering(isAllowWireframeRendering() && test(permissible, VMConstants.PERMISSION_CLIENT_WIREFRAME))
                .build();
    }

    private boolean test(@Nullable Permissible permissible, @NotNull String permission) {
        return permissible == null || permissible.hasPermission(permission);
    }

    @Override
    public boolean isAllowActivationKeybind() {
        return plugin.getConfig().getBoolean(KEY_CLIENT_ALLOW_ACTIVATION_KEYBIND, true);
    }

    @Override
    public boolean isAllowPatternSwitchingKeybind() {
        return plugin.getConfig().getBoolean(KEY_CLIENT_ALLOW_PATTERN_SWITCHING_KEYBIND, true);
    }

    @Override
    public boolean isAllowWireframeRendering() {
        return plugin.getConfig().getBoolean(KEY_CLIENT_ALLOW_WIREFRAME_RENDERING, true);
    }

    @NotNull
    @Override
    public PersistentStorageType getStorageType() {
        String typeString = plugin.getConfig().getString(KEY_STORAGE_TYPE);
        if (typeString == null) {
            return PersistentStorageType.SQLITE;
        }

        PersistentStorageType type = PersistentStorageType.getByName(typeString.toLowerCase());
        return type != null ? type : PersistentStorageType.NONE;
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
    public Collection<String> getAliasStrings() {
        return ImmutableList.copyOf(plugin.getConfig().getStringList(KEY_ALIASES));
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getGlobalBlockListKeys() {
        return ImmutableList.copyOf(plugin.getCategoriesConfig().asRawConfig().getStringList("All." + KEY_BLOCK_LIST));
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getDefinedCategoryIds() {
        Set<String> keys = plugin.getCategoriesConfig().asRawConfig().getKeys(false);
        keys.remove("All");
        return keys;
    }

    @NotNull
    @Override
    public ToolCategoryConfiguration getToolCategoryConfiguration(@NotNull String categoryId) {
        ConfigWrapper categoriesConfig = this.plugin.getCategoriesConfig();
        return new StandardToolCategoryConfiguration(categoryId, categoriesConfig, this);
    }

}
