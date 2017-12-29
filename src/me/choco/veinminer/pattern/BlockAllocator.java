package me.choco.veinminer.pattern;

import java.util.List;

import org.bukkit.block.Block;

import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinTool;

/**
 * A functional interface to assist in the allocation of vein mining patterns. For
 * application, see {@link VeinMiningPattern#createNewPattern(org.bukkit.NamespacedKey, BlockAllocator)}
 */
@FunctionalInterface
public interface BlockAllocator {
	
	/**
	 * Allocate the blocks that should be broken by the vein mining pattern. Note
	 * that the breaking of the blocks should not be handled by the pattern, but
	 * rather the plugin itself. This method serves primarily to search for valid
	 * blocks to break in a vein.
	 * <p>
	 * <b>NOTE:</b> If null is added to the "blocks" list, a NullPointerException
	 * will be thrown and the method will fail.
	 * 
	 * @param blocks a list of all blocks to break. Valid blocks should be added here.
	 * The "origin" block passed to this method will be added automatically
	 * @param origin the block where the vein mine was initiated
	 * @param tool the tool used to break the block
	 * @param alias an alias of the block being broken if one exists. May be null
	 * 
	 * @see VeinMiningPattern#allocateBlocks(List, Block, VeinTool, MaterialAlias)
	 */
	public void allocate(List<Block> blocks, Block origin, VeinTool tool, MaterialAlias alias);
	
}