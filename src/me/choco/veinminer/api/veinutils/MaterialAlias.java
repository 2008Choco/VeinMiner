package me.choco.veinminer.api.veinutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import me.choco.veinminer.utils.VeinMinerManager;

/**
 * Represents an aliasing between multiple {@link VeinBlock}s which VeinMiner
 * can recognise as a single material value when being vein mined
 */
public class MaterialAlias {
	
	private List<VeinBlock> blocks;
	
	/**
	 * Construct a new alias between varying vein blocks
	 * 
	 * @param blocks the blocks to alias
	 * 
	 * @see VeinBlock#getVeinminableBlock(Material, BlockData)
	 * @see VeinBlock#getVeinminableBlock(Material)
	 */
	public MaterialAlias(VeinBlock... blocks) {
		this.blocks = Arrays.asList(blocks);
	}
	
	/**
	 * Add a block to this alias
	 * 
	 * @param block the block to add
	 */
	public void addAlias(VeinBlock block) {
		Preconditions.checkArgument(block != null, "Cannot add a null alias");
		
		if (blocks.contains(block)) return;
		this.blocks.add(block);
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
	public VeinBlock addAlias(Material material, BlockData data) {
		Preconditions.checkArgument(material != null, "Cannot add a null material alias");
		
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
		return this.addAlias(material, null);
	}
	
	/**
	 * Remove a block from this alias
	 * 
	 * @param block the block to remove
	 */
	public void removeAlias(VeinBlock block) {
		this.blocks.remove(block);
	}
	
	/**
	 * Remove a material with byte data from this alias
	 * 
	 * @param material the material to remove
	 * @param data the data to remove
	 */
	public void removeAlias(Material material, BlockData data) {
		VeinBlock block = VeinBlock.getVeinminableBlock(material, data);
		this.removeAlias(block);
	}
	
	/**
	 * Remove a material with no byte data from this alias
	 * 
	 * @param material the material to remove
	 */
	public void removeAlias(Material material) {
		this.removeAlias(material, null);
	}
	
	/**
	 * Check whether a block is aliased under this material alias
	 * 
	 * @param block the block to check
	 * @return true if aliased
	 */
	public boolean isAliased(VeinBlock block) {
		return blocks.contains(block);
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
	public boolean isAliased(Material material, BlockData data) {
		return blocks.stream().anyMatch(b -> b.getMaterial() == material && (!b.hasSpecficData() || b.getData().equals(data)));
	}
	
	/**
	 * Check whether a material with no byte data is aliased under this
	 * material alias
	 * 
	 * @param material the material to check
	 * @return true if aliased
	 */
	public boolean isAliased(Material material) {
		return blocks.stream().anyMatch(b -> b.getMaterial() == material);
	}
	
	/**
	 * Get all blocks that are considered under this alias
	 * 
	 * @return all aliased blocks
	 */
	public List<VeinBlock> getAliasedBlocks() {
		return new ArrayList<>(blocks);
	}

	@Override
	public int hashCode() {
		return 31 * blocks.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof MaterialAlias)) return false;
		
		MaterialAlias alias = (MaterialAlias) object;
		return blocks.equals(alias.blocks);
	}
	
}