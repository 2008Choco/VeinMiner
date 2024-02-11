package wtf.choco.veinminer.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * A configuration contract for a {@link VeinMinerToolCategory}'s configurable values.
 * <p>
 * If this category configuration does not explicitly set a value for a {@link
 * VeinMiningConfiguration} method, it will default to the global value as set by the
 * VeinMiningConfiguration.
 * <p>
 * Unless otherwise specified, all values obtained via this contract are updated in real
 * time and will always return the values that were last loaded into memory via
 * {@link JavaPlugin#reloadConfig()} and {@link ConfigWrapper#reload()}.
 */
public interface ToolCategoryConfiguration extends VeinMiningConfiguration {

    /**
     * Get this category's configured priority.
     *
     * @return the priority
     *
     * @see VeinMinerToolCategory#getPriority()
     */
    public int getPriority();

    /**
     * Get this category's required NBT value.
     *
     * @return the NBT value, or null if not set
     *
     * @see VeinMinerToolCategory#getNBTValue()
     */
    @Nullable
    public String getNBTValue();

    /**
     * Set the {@link List} of item {@link Material Materials} that are represented by this
     * category.
     *
     * @param items the item list
     */
    public void setItems(@NotNull List<Material> items);

    /**
     * Get an unmodifiable {@link Collection} of Strings, each entry a Minecraft item key, for
     * the list of items that are represented by this category.
     *
     * @return the item keys
     *
     * @apiNote The returned Collection is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    @Unmodifiable
    public Collection<String> getItemKeys();

    /**
     * Set the {@link BlockList} for this category.
     *
     * @param blockList the block list
     */
    public void setBlockListKeys(@NotNull BlockList blockList);

    /**
     * Get an unmodifiable {@link Collection} of Strings, each entry a Minecraft block key, for
     * the list of blocks that are vein mineable with this category.
     *
     * @return the block list keys
     *
     * @apiNote The returned Collection is a snapshot of current values and, unlike other methods
     * in this configuration, will not be updated when this configuration is reloaded!
     */
    @NotNull
    @Unmodifiable
    public Collection<String> getBlockListKeys();

    /**
     * {@inheritDoc}.
     * <p>
     * If this category configuration does not explicitly set a value, it will default to the
     * global value as set by {@link VeinMinerConfiguration#isRepairFriendly()}.
     */
    @Override
    public boolean isRepairFriendly();

    /**
     * {@inheritDoc}.
     * <p>
     * If this category configuration does not explicitly set a value, it will default to the
     * global value as set by {@link VeinMinerConfiguration#getMaxVeinSize()}.
     */
    @Override
    public int getMaxVeinSize();

    /**
     * {@inheritDoc}.
     * <p>
     * If this category configuration does not explicitly set a value, it will default to the
     * global value as set by {@link VeinMinerConfiguration#getCost()}.
     */
    @Override
    public double getCost();

    /**
     * {@inheritDoc}.
     * <p>
     * If this category configuration does not explicitly set a value, it will default to the
     * global value as set by {@link VeinMinerConfiguration#isDisabledWorld(String)}.
     */
    @Override
    public boolean isDisabledWorld(@NotNull String worldName);

    /**
     * {@inheritDoc}.
     * <p>
     * If this category configuration does not explicitly set a value, it will default to the
     * global value as set by {@link VeinMinerConfiguration#isDisabledWorld(World)}.
     */
    @Override
    public default boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    /**
     * {@inheritDoc}.
     * <p>
     * If this category configuration does not explicitly set a value, it will default to the
     * global value as set by {@link VeinMinerConfiguration#getDisabledWorlds()}.
     */
    @Override
    @NotNull
    @Unmodifiable
    public Set<String> getDisabledWorlds();

}
