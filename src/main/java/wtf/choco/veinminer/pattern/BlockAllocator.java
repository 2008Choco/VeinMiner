package wtf.choco.veinminer.pattern;

import java.util.Set;

import org.bukkit.block.Block;

import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;

/**
 * A functional interface to assist in the allocation of vein mining patterns. For application, see
 * {@link VeinMiningPattern#createNewPattern(org.bukkit.NamespacedKey, BlockAllocator)}.
 */
@FunctionalInterface
public interface BlockAllocator {

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
	 *
	 * @see VeinMiningPattern#allocateBlocks(Set, VeinBlock, Block, ToolCategory, MaterialAlias)
	 */
	public void allocate(Set<Block> blocks, VeinBlock type, Block origin, ToolCategory tool, MaterialAlias alias);

}