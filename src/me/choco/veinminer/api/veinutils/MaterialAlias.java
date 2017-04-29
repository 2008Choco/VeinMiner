package me.choco.veinminer.api.veinutils;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.utils.VeinMinerManager;

/**
 * Represents an aliasing between multiple {@link VeinBlock}s which VeinMiner
 * can recognise as a single material value when being vein mined
 */
public class MaterialAlias {
	
	private static final VeinMinerManager MANAGER = VeinMiner.getPlugin().getVeinMinerManager();
	
	private final VeinBlock[] blocks;
	
	/**
	 * Construct a new alias between varying vein blocks
	 * 
	 * @param blocks - The blocks to alias
	 * 
	 * @see VeinMinerManager#getVeinminableBlock(Material, byte)
	 * @see VeinMinerManager#getVeinminableBlock(Material)
	 */
	public MaterialAlias(VeinBlock... blocks) {
		this.blocks = blocks;
	}
	
	/**
	 * Add a block to this alias
	 * 
	 * @param block - The block to add
	 */
	public void addAlias(VeinBlock block) {
		if (ArrayUtils.contains(blocks, block)) return;
		ArrayUtils.add(blocks, block);
	}
	
	/**
	 * Add a material and its byte data to this alias. If the VeinBlock does
	 * not already exist, it will be registered to the {@link VeinMinerManager}
	 * with the same {@link VeinTool}s as the dominant {@link VeinBlock}
	 * 
	 * @param material - The material to add
	 * @param data - The data to add
	 * 
	 * @return the newly aliased added
	 */
	public VeinBlock addAlias(Material material, byte data) {
		VeinBlock block = MANAGER.getVeinminableBlock(material, data);
		if (block == null) {
			block = new VeinBlock(material, data);
			
			if (blocks.length > 0) {
				VeinBlock original = this.blocks[0];
				block.addMineableBy(original.getMineableBy());
			}
			
			MANAGER.registerVeinminableBlock(block);
		}
		
		this.addAlias(block);
		return block;
	}
	
	/**
	 * Add a material with no byte data to this alias. If the VeinBlock does
	 * not already exist, it will be registered to the {@link VeinMinerManager}
	 * with the same {@link VeinTool}s as the dominant {@link VeinBlock}
	 * 
	 * @param material - The material to add
	 * @return the newly aliased added
	 */
	public VeinBlock addAlias(Material material) {
		return this.addAlias(material, (byte) -1);
	}
	
	/**
	 * Remove a block from this alias
	 * 
	 * @param block - The block to remove
	 */
	public void removeAlias(VeinBlock block) {
		if (!ArrayUtils.contains(blocks, block)) return;
		ArrayUtils.removeElement(blocks, block);
	}
	
	/**
	 * Remove a material with byte data from this alias
	 * 
	 * @param material - The material to remove
	 * @param data - The data to remove
	 */
	public void removeAlias(Material material, byte data) {
		VeinBlock block = MANAGER.getVeinminableBlock(material, data);
		if (block == null) return;
		
		this.removeAlias(block);
	}
	
	/**
	 * Remove a material with no byte data from this alias
	 * 
	 * @param material - The material to remove
	 */
	public void removeAlias(Material material) {
		this.removeAlias(material, (byte) -1);
	}
	
	/**
	 * Check whether a block is aliased under this material alias
	 * 
	 * @param block - The block to check
	 * @return true if aliased
	 */
	public boolean isAliased(VeinBlock block) {
		return ArrayUtils.contains(blocks, block);
	}
	
	/**
	 * Check whether a material with byte data is aliased under this
	 * material alias
	 * 
	 * @param material - The material to check
	 * @param data - The data to check
	 * 
	 * @return true if aliased
	 */
	public boolean isAliased(Material material, byte data) {
		return Arrays.stream(blocks).anyMatch(b -> b.getMaterial() == material && b.getData() == data);
	}
	
	/**
	 * Check whether a material with no byte data is aliased under this
	 * material alias
	 * 
	 * @param material - The material to check
	 * @return true if aliased
	 */
	public boolean isAliased(Material material) {
		return Arrays.stream(blocks).anyMatch(b -> b.getMaterial() == material);
	}
	
	/**
	 * Get all blocks that are considered under this alias
	 * 
	 * @return all aliased blocks
	 */
	public VeinBlock[] getAliasedBlocks() {
		return Arrays.copyOf(blocks, blocks.length);
	}
	
}