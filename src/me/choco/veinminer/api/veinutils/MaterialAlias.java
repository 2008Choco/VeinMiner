package me.choco.veinminer.api.veinutils;

import java.util.Arrays;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;

import me.choco.veinminer.utils.VeinMinerManager;

/**
 * Represents an aliasing between multiple {@link VeinBlock}s which VeinMiner
 * can recognise as a single material value when being vein mined
 */
public class MaterialAlias {
	
	private VeinBlock[] blocks;
	
	/**
	 * Construct a new alias between varying vein blocks
	 * 
	 * @param blocks the blocks to alias
	 * 
	 * @see VeinBlock#getVeinminableBlock(Material, byte)
	 * @see VeinBlock#getVeinminableBlock(Material)
	 */
	public MaterialAlias(VeinBlock... blocks) {
		this.blocks = blocks;
	}
	
	/**
	 * Add a block to this alias
	 * 
	 * @param block the block to add
	 */
	public void addAlias(VeinBlock block) {
		Preconditions.checkArgument(block != null, "Cannot add a null alias");
		
		if (ArrayUtils.contains(blocks, block)) return;
		this.blocks = ArrayUtils.add(blocks, block);
	}
	
	/**
	 * Add a material and its byte data to this alias. If the VeinBlock does
	 * not already exist, it will be registered to the {@link VeinMinerManager}
	 * with the same {@link VeinTool}s as the dominant {@link VeinBlock}
	 * 
	 * @param material the material to add
	 * @param data the data to add
	 * 
	 * @return the newly aliased added
	 */
	public VeinBlock addAlias(Material material, byte data) {
		Preconditions.checkArgument(material != null, "Cannot add a null material alias");
		Preconditions.checkArgument(data >= -1, "Data values lower than 0 are unsupported (excluding the wildcard, -1)");
		
		VeinBlock block = VeinBlock.getVeinminableBlock(material, data);
		
		this.addAlias(block);
		return block;
	}
	
	/**
	 * Add a material with no byte data to this alias. If the VeinBlock does
	 * not already exist, it will be registered to the {@link VeinMinerManager}
	 * with the same {@link VeinTool}s as the dominant {@link VeinBlock}
	 * 
	 * @param material the material to add
	 * @return the newly aliased added
	 */
	public VeinBlock addAlias(Material material) {
		return this.addAlias(material, (byte) -1);
	}
	
	/**
	 * Remove a block from this alias
	 * 
	 * @param block the block to remove
	 */
	public void removeAlias(VeinBlock block) {
		if (!ArrayUtils.contains(blocks, block)) return;
		this.blocks = ArrayUtils.removeElement(blocks, block);
	}
	
	/**
	 * Remove a material with byte data from this alias
	 * 
	 * @param material the material to remove
	 * @param data the data to remove
	 */
	public void removeAlias(Material material, byte data) {
		VeinBlock block = VeinBlock.getVeinminableBlock(material, data);
		this.removeAlias(block);
	}
	
	/**
	 * Remove a material with no byte data from this alias
	 * 
	 * @param material the material to remove
	 */
	public void removeAlias(Material material) {
		this.removeAlias(material, (byte) -1);
	}
	
	/**
	 * Check whether a block is aliased under this material alias
	 * 
	 * @param block the block to check
	 * @return true if aliased
	 */
	public boolean isAliased(VeinBlock block) {
		return ArrayUtils.contains(blocks, block);
	}
	
	/**
	 * Check whether a material with byte data is aliased under this
	 * material alias
	 * 
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if aliased
	 */
	public boolean isAliased(Material material, byte data) {
		return Arrays.stream(blocks).anyMatch(b -> b.getMaterial() == material && (!b.hasSpecficData() || b.getData() == data));
	}
	
	/**
	 * Check whether a material with no byte data is aliased under this
	 * material alias
	 * 
	 * @param material the material to check
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

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(blocks);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof MaterialAlias)) return false;
		
		MaterialAlias alias = (MaterialAlias) object;
		return Arrays.equals(blocks, alias.blocks);
	}
	
}