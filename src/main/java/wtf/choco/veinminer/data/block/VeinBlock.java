package wtf.choco.veinminer.data.block;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * Represents a block that may be vein mined. These blocks may or may not contain
 * additional data or wildcards, therefore the result of {@link #encapsulates(Material)}
 * (and its overrides) may vary based on the implementation and whether or not the block
 * has additional data (see {@link #hasSpecificData()}).
 *
 * @author Parker Hawke - 2008Choco
 */
public interface VeinBlock {

	/**
	 * Get the Bukkit {@link Material} represented by this block
	 *
	 * @return the material type
	 */
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
	public boolean encapsulates(Block block);

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
	public boolean encapsulates(BlockData data);

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
	public boolean encapsulates(Material material);

	/**
	 * Get this VeinBlock instance as a readable data String. Similar to how
	 * {@link BlockData#getAsString()} returns a human-readable representation of block data,
	 * this will return a human-readable representation of the vein block based on its defined
	 * data (if any). It will be under a similar format as the aforementioned method.
	 *
	 * @return the human-readable data string
	 */
	public String asDataString();

	/**
	 * Get a VeinBlock based on type with no additional block states.
	 *
	 * @param material the material for which to get a VeinBlock instance
	 *
	 * @return the VeinBlock instance
	 */
	public static VeinBlock get(Material material) {
		Preconditions.checkArgument(material != null, "Cannot get VeinBlock with null type");
		return new VeinBlockMaterial(material);
	}

	/**
	 * Get a VeinBlock based on block data with optional states and the raw data from which
	 * the BlockData was derived.
	 *
	 * @param data the block data for which to get a VeinBlock instance
	 * @param raw the raw, human-readable BlockData String (including material). For example,
	 * "minecraft:chest[waterlogged=true]" would be valid raw data. No additional checks are
	 * performed to guarantee the data and the raw data provided match.
	 *
	 * @return the VeinBlock instance
	 */
	public static VeinBlock get(BlockData data, String raw) {
		Preconditions.checkArgument(data != null, "Cannot get VeinBlock with null data");
		Validate.notEmpty(raw, "Raw data must not be empty");

		return new VeinBlockDatable(data, raw);
	}

}