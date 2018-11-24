package wtf.choco.veinminer.pattern;

import java.util.Set;

import com.google.common.base.Preconditions;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import wtf.choco.veinminer.api.MaterialAlias;
import wtf.choco.veinminer.api.VeinTool;
import wtf.choco.veinminer.api.blocks.VeinBlock;

/**
 * Represents a mining algorithm capable of allocating which blocks should be broken by VeinMiner
 * when a successful vein mine occurs. It is recommended that implementations of VeinMiningPattern
 * are singleton instances, although this is not a requirement.
 */
public interface VeinMiningPattern extends Keyed {

	/**
	 * Allocate the blocks that should be broken by the vein mining pattern. Note that the breaking
	 * of the blocks should not be handled by the pattern, but rather the plugin itself. This method
	 * serves primarily to search for valid blocks to break in a vein.
	 * <p>
	 * <b>NOTE:</b> If null is added to the "blocks" set, a NullPointerException will be thrown and
	 * the method will fail.
	 *
	 * @param blocks a set of all blocks to break. Valid blocks should be added here. The "origin"
	 * block passed to this method will be added automatically
	 * @param type the type of VeinBlock being vein mined
	 * @param origin the block where the vein mine was initiated
	 * @param tool the tool used to break the block
	 * @param alias an alias of the block being broken if one exists. May be null
	 */
	public void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, VeinTool tool, MaterialAlias alias);

	/**
	 * Allocate the blocks that should be broken by the vein mining pattern. Note that the breaking
	 * of the blocks should not be handled by the pattern, but rather the plugin itself. This method
	 * serves primarily to search for valid blocks to break in a vein.
	 * <p>
	 * <b>NOTE:</b> If null is added to the "blocks" set, a NullPointerException will be thrown and
	 * the method will fail.
	 *
	 * @param blocks a set of all blocks to break. Valid blocks should be added here. The "origin"
	 * block passed to this method will be added automatically
	 * @param type the type of VeinBlock being vein mined
	 * @param origin the block where the vein mine was initiated
	 * @param tool the tool used to break the block
	 */
	public default void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, VeinTool tool) {
		this.allocateBlocks(blocks, type, origin, tool, null);
	}

	/**
	 * Create a new VeinMiningPattern using a custom {@link BlockAllocator}.
	 *
	 * @param key the key of the vein mining pattern. Must be unique and not null
	 * @param blockAllocator the allocator to compute breakable blocks. Must not be null
	 *
	 * @return the resulting VeinMiningPattern instance
	 */
	public static VeinMiningPattern createNewPattern(NamespacedKey key, BlockAllocator blockAllocator) {
		Preconditions.checkNotNull(key, "Pattern must not have a null key");
		Preconditions.checkNotNull(blockAllocator, "Block computer must not be null");

		return new VeinMiningPattern() {
			@Override
			public void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, VeinTool tool, MaterialAlias alias) {
				blockAllocator.allocate(blocks, type, origin, tool, alias);
			}

			@Override
			public NamespacedKey getKey() {
				return key;
			}
		};
	}

}