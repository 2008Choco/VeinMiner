package wtf.choco.veinminer.config;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.data.PersistentStorageType;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.player.ActivationStrategy;

/**
 * A configuration contract for all of VeinMiner's configurable values.
 * <p>
 * Unless otherwise specified, all values obtained via this contract are updated in real
 * time and will always return the values that were last loaded into memory via
 * {@link JavaPlugin#reloadConfig()} and {@link ConfigWrapper#reload()}.
 */
public interface VeinMinerConfiguration extends VeinMiningConfiguration {

    /**
     * Get whether or not metrics are enabled.
     * <p>
     * If this value returns true, bStats Metrics will be used to send anonymous data to
     * <a href="https://bstats.org/plugin/bukkit/VeinMiner">https://bstats.org/plugin/bukkit/VeinMiner</a>.
     *
     * @return true if metrics are enabled, false if disabled
     */
    public boolean isMetricsEnabled();

    /**
     * Get whether or not an update check should be performed when VeinMiner is enabled.
     *
     * @return true if an update check should be performed, false otherwise
     */
    public boolean isPerformUpdateChecks();

    /**
     * Get the default {@link ActivationStrategy} that will be used for any players that
     * do not have an activation strategy explicitly set.
     *
     * @return the default activation strategy
     */
    @NotNull
    public ActivationStrategy getDefaultActivationStrategy();

    /**
     * Get the default {@link VeinMiningPattern} that will be used for any players that
     * do not have a vein mining pattern explicitly set.
     *
     * @return the default vein mining pattern
     */
    @NotNull
    public VeinMiningPattern getDefaultVeinMiningPattern();

    /**
     * Get whether or not items should be collected and dropped at the origin location.
     * <p>
     * If this value returns true, after a player completes a vein mine, all the item drops
     * that occurred as a result of all the blocks being broken will be dropped at the location
     * where the original block was broken. If false, the items will drop individually at the
     * block's broken position, as though the player broke each block separately.
     *
     * @return true if items will be collected at the source, false if dropped at each block's
     * location
     */
    public boolean isCollectItemsAtSource();

    /**
     * Get whether or not experience should be collected and dropped at the origin location.
     * <p>
     * If this value returns true, after a player completes a vein mine, all the experience that
     * would have dropped as a result of all the blocks being broken will be dropped at the location
     * where the original block was broken. If false, the experience orbs will drop individually at
     * the block's broken position, as though the player broke each block separately.
     * <p>
     * If this value is not explicitly set, it will default to {@link #isCollectItemsAtSource()}.
     *
     * @return true if experience will be collected at the source, false if dropped at each block's
     * location
     */
    public boolean isCollectExperienceAtSource();

    /**
     * Get whether or not damage should be applied to the tool used while vein mining <strong>only
     * </strong> for the first block that was broken, not any subsequent blocks mined in the vein.
     * <p>
     * If this value returns true, standard vanilla block break item damage logic (taking into account
     * the unbreaking enchantment and any other damage modifiers, etc.) will only be calculated for the
     * first block that was broken. If false, then durability will be applied to tools with vanilla
     * block break logic as though the player broke each block separately.
     *
     * @return true to only damage for the first block, false to apply standard vanilla damage for
     * each broken block
     */
    public boolean isOnlyDamageOnFirstBlock();

    /**
     * Get whether or not McMMO's experience system should be nerfed while vein mining.
     * <p>
     * If this value returns true, McMMO experience will only be yielded for the initial block
     * break. If false, every block broken in the vein mine will yield mining experience.
     *
     * @return true to nerf McMMO experience yield, false to behave as normal
     */
    public boolean isNerfMcMMO();

    /**
     * Check whether or not vein mining is disabled when the player activates it while in the
     * specified {@link GameMode}.
     *
     * @param gameMode the game mode
     *
     * @return true if vein mining is disabled in the provided game mode, false if enabled
     */
    public boolean isDisabledGameMode(@NotNull GameMode gameMode);

    /**
     * Get an unmodifiable {@link Set} of all game modes in which vein miner is disabled.
     *
     * @return all disabled game modes
     */
    @NotNull
    @Unmodifiable
    public Set<GameMode> getDisabledGameModes();

    /**
     * Get the hunger modifier to apply to a player for each block broken when vein mining. This
     * is not a hunger <em>value</em>, but rather a modifier to apply on top of traditional hunger
     * while vein mining as a punishment for its efficiency.
     *
     * @return the hunger modifier
     */
    public float getHungerModifier();

    /**
     * Get the minimum amount of food required for a player to vein mine.
     * <p>
     * This food level is checked in two different situations:
     * <ol>
     *   <li>When a player tries to start vein mining, they must have at least this amount of hunger
     *       to successfully initiate a vein mine.
     *   <li>While a player is mining, if the food level goes below the minimum amount, vein mining
     *       will stop immediately.
     * </ol>
     *
     * @return the minimum food level
     */
    public int getMinimumFoodLevel();

    /**
     * Get the default {@link ClientConfig}.
     *
     * @return the default client configuration
     *
     * @see #isAllowActivationKeybind()
     * @see #isAllowPatternSwitchingKeybind()
     * @see #isAllowWireframeRendering()
     *
     * @apiNote The returned ClientConfig is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    public ClientConfig getClientConfiguration();

    /**
     * Create a {@link ClientConfig} using the values specified in this configuration as well as the
     * permissions calculated by the given {@link Permissible} object. If any individual option is
     * {@code true}, it will be {@literal &&}'d with whether or not {@link Permissible#hasPermission(String)}
     * for the permission associated with each value.
     *
     * @param permissible the permissible object
     *
     * @return the client configuration relative to the given Permissible object
     *
     * @see #isAllowActivationKeybind()
     * @see #isAllowPatternSwitchingKeybind()
     * @see #isAllowWireframeRendering()
     *
     * @apiNote The returned ClientConfig is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    public ClientConfig getClientConfiguration(@NotNull Permissible permissible);

    /**
     * Get whether or not clients with the client-sided mod are allowed to use their own configured
     * keybind to activate vein miner.
     * <p>
     * <strong>NOTE:</strong> This value is only relevant for players that have the client-sided mod
     * installed.
     *
     * @return true if allowed to use the activation keybind, false otherwise
     */
    public boolean isAllowActivationKeybind();

    /**
     * Get whether or not clients with the client-sided mod are allowed to use their own configured
     * keybind to switch between registered vein mining patterns.
     * <p>
     * <strong>NOTE:</strong> This value is only relevant for players that have the client-sided mod
     * installed.
     *
     * @return true if allowed to use the pattern switching keybinds, false otherwise
     */
    public boolean isAllowPatternSwitchingKeybind();

    /**
     * Get whether or not clients with the client-sided mod are allowed to render a wireframe outline
     * around blocks they are able to vein mine. The wireframe shape is calculated by the server.
     * <p>
     * <strong>NOTE:</strong> This value is only relevant for players that have the client-sided mod
     * installed.
     *
     * @return true if allowed to render a wireframe, false otherwise
     */
    public boolean isAllowWireframeRendering();

    /**
     * Get the type of storage to be used when storing persistent data.
     *
     * @return the storage type
     */
    @NotNull
    public PersistentStorageType getStorageType();

    /**
     * Get the {@link File} directory in which JSON data is stored.
     *
     * @return the JSON storage directory
     */
    @Nullable
    public File getJsonStorageDirectory();

    /**
     * Get the host represented as a String for the MySQL server in which persistent data will be
     * stored.
     *
     * @return the MySQL host
     */
    @Nullable
    public String getMySQLHost();

    /**
     * Get the port for the MySQL server in which persistent data will be stored.
     *
     * @return the MySQL port
     */
    public int getMySQLPort();

    /**
     * Get the username for the MySQL instance in which persistent data will be stored.
     *
     * @return the MySQL username
     */
    @Nullable
    public String getMySQLUsername();

    /**
     * Get the password for the MySQL instance in which persistent data will be stored.
     *
     * @return the MySQL password
     */
    @Nullable
    public String getMySQLPassword();

    /**
     * Get the database for the MySQL instance in which persistent data will be stored.
     *
     * @return the MySQL database
     */
    @Nullable
    public String getMySQLDatabase();

    /**
     * Get the prefix to use before all MySQL tables VeinMiner will create for persistent data.
     *
     * @return the MySQL port
     */
    @Nullable
    public String getMySQLTablePrefix();

    /**
     * Get an unmodifiable {@link Collection} of Strings containing the aliases that need to
     * be parsed by VeinMiner to create a {@link BlockList}. Each returned string entry should
     * be a pseudo "array" of Minecraft block keys separated by {@code ';'}s.
     *
     * @return the alias strings
     *
     * @apiNote The returned Collection is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    @Unmodifiable
    public Collection<String> getAliasStrings();

    /**
     * Get an unmodifiable {@link Collection} of Strings, each entry a Minecraft block key, for
     * the list of blocks that are vein mineable with all categories.
     *
     * @return the global block list keys
     *
     * @apiNote The returned Collection is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    @Unmodifiable
    public Collection<String> getGlobalBlockListKeys();

    /**
     * Get an unmodifiable {@link Collection} of Strings, each entry a unique id for a category
     * defined in the categories.yml. This will always exclude "All" which is not a registerable
     * category.
     *
     * @return all defined category ids
     *
     * @apiNote The returned Collection is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    @Unmodifiable
    public Collection<String> getDefinedCategoryIds();

    /**
     * Get a {@link ToolCategoryConfiguration} for the category with the given id.
     * <p>
     * The returned instance will be updated automatically if the configuration is ever reloaded.
     * A new instance does not need to be returned upon reloading. Existing instances may be
     * reused but still have their values reflect the newly reloaded ones.
     *
     * @param categoryId the category id whose configuration to get
     *
     * @return the tool category configuration
     */
    @NotNull
    public ToolCategoryConfiguration getToolCategoryConfiguration(@NotNull String categoryId);

}
