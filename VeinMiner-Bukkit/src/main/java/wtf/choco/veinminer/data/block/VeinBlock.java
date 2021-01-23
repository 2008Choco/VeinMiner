package wtf.choco.veinminer.data.block;

import com.google.common.base.Preconditions;

import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;

/**
 * Represents a block that may be vein mined. These blocks may or may not contain
 * additional data or wildcards, therefore the result of {@link #encapsulates(Material)}
 * (and its overrides) may vary based on the implementation and whether or not the block
 * has additional data (see {@link #hasSpecificData()}).
 *
 * @author Parker Hawke - 2008Choco
 */
public interface VeinBlock extends Comparable<VeinBlock> {

    /**
     * Get the Bukkit {@link Material} represented by this block
     *
     * @return the material type
     */
    @NotNull
    public Material getType();

    /**
     * Check whether or not this block includes more specific block data (for example,
     * "minecraft:chest" would return false whereas "minecraft:chest[facing=north]"
     * would return true due to the specified "facing" block state.
     *
     * @return true if specific data is defined, false if wildcarded to type only
     */
    public boolean hasSpecificData();

    /**
     * Get the Bukkit {@link BlockData} represented by this block. If this VeinBlock
     * has no specific data, this method will return the equivalent of
     * {@link Material#createBlockData()} with no additional block state data.
     *
     * @return the block data
     */
    @NotNull
    public BlockData getBlockData();

    /**
     * Check whether or not the provided block is encapsulated by this VeinBlock. If
     * encapsulated, the provided block may be considered valid to vein mine.
     * <p>
     * The result of this method may vary based on whether or not this block has specific
     * data. If specific data is defined, any non-specified states in the underlying
     * BlockData will be ignored... only specified states will be compared. If otherwise,
     * only the block's type will be compared.
     *
     * @param block the block to check
     *
     * @return true if encapsulated and valid to vein mine for this type, false otherwise
     *
     * @see #encapsulates(BlockData)
     * @see #encapsulates(Material)
     */
    public boolean encapsulates(@NotNull Block block);

    /**
     * Check whether or not the provided data is encapsulated by this VeinBlock. If
     * encapsulated, the provided data may be considered valid to vein mine.
     * <p>
     * The result of this method may vary based on whether or not this block has specific
     * data. If specific data is defined, any non-specified states in the underlying
     * BlockData will be ignored... only specified states will be compared. If otherwise,
     * only the data's type will be compared.
     *
     * @param data the data to check
     *
     * @return true if encapsulated and valid to vein mine for this type, false otherwise
     *
     * @see #encapsulates(Block)
     * @see #encapsulates(Material)
     */
    public boolean encapsulates(@NotNull BlockData data);

    /**
     * Check whether or not the provided material is encapsulated by this VeinBlock. If
     * encapsulated, the provided material may be considered valid to vein mine.
     * <p>
     * The result of this method will vary based on whether or not this block has specific
     * data. If specific data is defined, this method will always return false... because
     * materials are stateless, they cannot possible match a vein block with specified data.
     * That being said, while this method may return false for identical materials,
     * {@link #getType()} when compared to the provided material will return true as expected.
     * In other words,
     * <p>
     * <code>veinblock.getType() == material</code> will return true.<br>
     * <code>veinblock.encapsulates(material)</code> will return false.
     * <p>
     * If otherwise, disregard the above, the block's type will be compared as expected.
     *
     * @param material the material to check
     *
     * @return true if encapsulated and valid to vein mine for this type, false otherwise
     *
     * @see #encapsulates(Block)
     * @see #encapsulates(BlockData)
     */
    public boolean encapsulates(@NotNull Material material);

    /**
     * Get this VeinBlock instance as a readable data String. Similar to how
     * {@link BlockData#getAsString()} returns a human-readable representation of block data,
     * this will return a human-readable representation of the vein block based on its defined
     * data (if any). It will be under a similar format as the aforementioned method.
     *
     * @return the human-readable data string
     */
    @NotNull
    public String asDataString();

    /**
     * Get whether or not this block is a wildcard.
     *
     * @return true if wildcard, false otherwise
     */
    public default boolean isWildcard() {
        return false;
    }

    @Override
    public default int compareTo(@Nullable VeinBlock other) {
        return (other != null) ? asDataString().compareTo(other.asDataString()) : 1;
    }

    /**
     * Get a VeinBlock based on type with no additional block states.
     *
     * @param material the material for which to get a VeinBlock instance
     *
     * @return the VeinBlock instance
     */
    @NotNull
    public static VeinBlock get(@NotNull Material material) {
        Preconditions.checkArgument(material != null, "Cannot get VeinBlock with null type");
        return BlockCache.MATERIAL.getOrCache(material, VeinBlockMaterial::new);
    }

    /**
     * Get a VeinBlock based on block data with a set of states.
     *
     * @param data the block data for which to get a VeinBlock instance
     *
     * @return the VeinBlock instance
     */
    @NotNull
    public static VeinBlock get(@NotNull BlockData data) {
        Preconditions.checkArgument(data != null, "Cannot get VeinBlock with null data");
        return BlockCache.BLOCK_DATA.getOrCache(data, VeinBlockDatable::new);
    }

    /**
     * Get a VeinBlock based on a String representation of its material and/or state.
     * If the format of the String is inconsistent with how Minecraft formats its states,
     * or if the type / (one or more of the) states are invalid or unknown, this method
     * will return null. An example of valid formats are as follows:
     * <pre>{@code
     * chest
     * minecraft:chest
     * minecraft:chest[waterlogged=true]
     * minecraft:chest[facing=north,waterlogged=true]
     * *
     * }</pre>
     *
     * @param value the value from which to get a VeinBlock instance.
     *
     * @return the parsed VeinBlock instance. null if malformed
     */
    @Nullable
    public static VeinBlock fromString(@NotNull String value) {
        if (value.equals("*")) {
            return wildcard();
        }

        Matcher matcher = VeinMiner.BLOCK_DATA_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        BlockData data;
        boolean specificData = (matcher.group(1) != null);

        try {
            data = Bukkit.createBlockData(matcher.group());
        } catch (IllegalArgumentException e) {
            return null;
        }

        return (specificData) ? VeinBlock.get(data) : VeinBlock.get(data.getMaterial());
    }

    /**
     * Get the wildcard {@link VeinBlock} instance.
     *
     * @return the wildcard instance
     */
    @NotNull
    public static VeinBlock wildcard() {
        return VeinBlockWildcard.INSTANCE;
    }

    /**
     * Clear the VeinBlock cache. This may slightly decrease performance until the cache returns
     * to a more populated state.
     */
    public static void clearCache() {
        BlockCache.clear();
    }

}
