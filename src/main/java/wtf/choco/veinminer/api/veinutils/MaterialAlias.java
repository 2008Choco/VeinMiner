package wtf.choco.veinminer.api.veinutils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.utils.VeinMinerManager;

/**
 * Represents an aliasing between multiple {@link VeinBlock}s which VeinMiner can recognise as a
 * single material value when being vein mined.
 */
public class MaterialAlias implements Iterable<VeinBlock>, Cloneable {
	
	private final Set<VeinBlock> blocks = new HashSet<>();
	
	/**
	 * Construct a new alias between varying vein blocks.
	 * 
	 * @param blocks the blocks to alias
	 * 
	 * @see VeinBlock#getVeinminableBlock(Material, BlockData)
	 * @see VeinBlock#getVeinminableBlock(Material)
	 */
	public MaterialAlias(VeinBlock... blocks) {
		for (VeinBlock block : blocks) {
			this.blocks.add(block);
		}
	}
	
	private MaterialAlias(Set<VeinBlock> blocks) {
		this.blocks.addAll(blocks);
	}
	
	/**
	 * Add a block to this alias.
	 * 
	 * @param block the block to add
	 */
	public void addAlias(VeinBlock block) {
		Preconditions.checkNotNull(block, "Cannot add a null alias");
		this.blocks.add(block);
	}
	
	/**
	 * Add a material and its block data to this alias. If the VeinBlock does not already exist, it
	 * will be registered to the {@link VeinMinerManager} with the same {@link VeinTool}s as the
	 * dominant {@link VeinBlock}.
	 * 
	 * @param material the material to add
	 * @param data the data to add
	 * 
	 * @return the newly aliased VeinBlock
	 */
	public VeinBlock addAlias(Material material, BlockData data) {
		Preconditions.checkNotNull(material, "Cannot add a null material alias");
		
		VeinBlock block = VeinBlock.getVeinminableBlock(material, data);
		if (block == null) {
			block = VeinBlock.registerVeinminableBlock(material, data, Iterables.get(blocks, 0).getMineableBy());
		}
		
		this.addAlias(block);
		return block;
	}
	
	/**
	 * Add a material with no specific block data to this alias. If the VeinBlock does not already exist,
	 * it will be registered to the {@link VeinMinerManager} with the same {@link VeinTool}s as the
	 * dominant {@link VeinBlock}.
	 * 
	 * @param material the material to add
	 * 
	 * @return the newly aliased added
	 */
	public VeinBlock addAlias(Material material) {
		return addAlias(material, null);
	}
	
	/**
	 * Remove a VeinBlock from this alias.
	 * 
	 * @param block the block to remove
	 */
	public void removeAlias(VeinBlock block) {
		this.blocks.remove(block);
	}
	
	/**
	 * Remove a material with specific block data from this alias.
	 * 
	 * @param material the material to remove
	 * @param data the data to remove
	 */
	public void removeAlias(Material material, BlockData data) {
		this.removeAlias(VeinBlock.getVeinminableBlock(material, data));
	}
	
	/**
	 * Remove a material without any specific block data from this alias.
	 * 
	 * @param material the material to remove
	 */
	public void removeAlias(Material material) {
		this.removeAlias(material, null);
	}
	
	/**
	 * Check whether a block is aliased under this material alias.
	 * 
	 * @param block the block to check
	 * 
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(VeinBlock block) {
		return blocks.contains(block);
	}
	
	/**
	 * Check whether a material with specific block data is aliased under this material alias.
	 * 
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(Material material, BlockData data) {
		return blocks.stream().anyMatch(b -> b.getMaterial() == material && (!b.hasSpecficData() || b.getData().equals(data)));
	}
	
	/**
	 * Check whether a material with no specific block data is aliased under this material alias.
	 * 
	 * @param material the material to check
	 * 
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(Material material) {
		return blocks.stream().anyMatch(b -> b.getMaterial() == material);
	}
	
	/**
	 * Get all blocks that are considered under this alias. A copy of the set is returned,
	 * therefore any changes made to the returned set will not affect this MaterialAlias.
	 * 
	 * @return all aliased blocks
	 */
	public Set<VeinBlock> getAliasedBlocks() {
		return new HashSet<>(blocks);
	}
	
	@Override
	public Iterator<VeinBlock> iterator() {
		return blocks.iterator();
	}
	
	@Override
	public MaterialAlias clone() {
		return new MaterialAlias(blocks);
	}
	
	@Override
	public int hashCode() {
		return 31 * blocks.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof MaterialAlias)) return false;
		
		MaterialAlias alias = (MaterialAlias) object;
		return blocks.equals(alias.blocks);
	}
	
}