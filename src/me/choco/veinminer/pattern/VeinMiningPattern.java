package me.choco.veinminer.pattern;

import java.util.List;

import com.google.common.base.Preconditions;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinTool;

/**
 * Represents a mining algorithm capable of computing which blocks should be broken
 * by VeinMiner when a successful veinmine will occur
 */
public interface VeinMiningPattern extends Keyed {
	
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
	 */
	public void allocateBlocks(List<Block> blocks, Block origin, VeinTool tool, MaterialAlias alias);
	
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
	 */
	public default void allocateBlocks(List<Block> blocks, Block origin, VeinTool tool) {
		this.allocateBlocks(blocks, origin, tool, null);
	}
	
	/**
	 * Create a new VeinMiningPattern using a custom {@link BlockAllocator}
	 * 
	 * @param key the key of the vein mining pattern. Must be unique and not null
	 * @param blockComputer the computer to allocate breakable blocks. Must not be null
	 * 
	 * @return the resulting VeinMiningPattern instance
	 */
	public static VeinMiningPattern createNewPattern(NamespacedKey key, BlockAllocator blockAllocator) {
		Preconditions.checkArgument(key != null, "Pattern must not have a null key");
		Preconditions.checkArgument(blockAllocator != null, "Block computer must not be null");
		
		return new VeinMiningPattern() {
			@Override
			public void allocateBlocks(List<Block> blocks, Block origin, VeinTool tool, MaterialAlias alias) {
				blockAllocator.allocate(blocks, origin, tool, alias);
			}
			
			@Override
			public NamespacedKey getKey() {
				return key;
			}
		};
	}
	
}