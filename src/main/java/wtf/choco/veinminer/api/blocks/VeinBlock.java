package wtf.choco.veinminer.api.blocks;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.api.VeinTool;

/**
 * Represents block mineable by VeinMiner. A VeinBlock may or may not possess additional data
 * beyond a {@link Material} type.
 */
public class VeinBlock {
	
	private final Set<VeinTool> tools = EnumSet.noneOf(VeinTool.class);
	
	private final Material type;
	private final BlockData data;
	
	public VeinBlock(BlockData data, VeinTool... tools) {
		this(data);
		
		for (VeinTool tool : tools) {
			this.tools.add(tool);
		}
	}
	
	public VeinBlock(Material type, VeinTool... tools) {
		this(type);
		
		for (VeinTool tool : tools) {
			this.tools.add(tool);
		}
	}
	
	public VeinBlock(BlockData data) {
		this.type = data.getMaterial();
		this.data = data;
	}
	
	public VeinBlock(Material type) {
		this.type = type;
		this.data = type.createBlockData("[]");
	}
	
	/**
	 * Get the type of {@link Material} represented by this VeinBlock instance.
	 * 
	 * @return the Bukkit material
	 */
	public Material getType() {
		return type;
	}
	
	/**
	 * Set whether the specified {@link VeinTool} is capable of mining this VeinBlock with vein miner
	 * or not.
	 * 
	 * @param tool the tool to set
	 * @param mineable true if should be mineable, false otherwise
	 */
	public void setVeinmineableBy(VeinTool tool, boolean mineable) {
		if (mineable) {
			this.tools.add(tool);
		} else {
			this.tools.remove(tool);
		}
	}
	
	/**
	 * Check whether the specified {@link VeinTool} is capable of mining this VeinBlock with vein miner
	 * or not.
	 * 
	 * @param tool the tool to check
	 * 
	 * @return true if mineable, false otherwise
	 */
	public boolean isVeinmineableBy(VeinTool tool) {
		return tools.contains(tool);
	}
	
	/**
	 * Get an immutable set of all tools capable of vein mining this VeinBlock.
	 * 
	 * @return all tools
	 */
	public Set<VeinTool> getVeinmineableBy() {
		return Collections.unmodifiableSet(tools);
	}
	
	/**
	 * Get the specific data associated with this block, if any. Will never be null. The returned BlockData
	 * instance is immutable and will have no effect on the underlying block data
	 * 
	 * @return the associated data, if any
	 */
	public BlockData getData() {
		return data.clone();
	}
	
	/**
	 * Check whether the provided block is encapsulated by this VeinBlock or not.
	 * 
	 * @param block the block to check
	 * 
	 * @return true if similar, false otherwise
	 */
	@SuppressWarnings("deprecation") // Draft API
	public boolean isSimilar(Block block) {
		return block.getBlockData().matches(data);
	}
	
	/**
	 * Check whether the provided block data is encapsulated by this VeinBlock or not.
	 * 
	 * @param blockData the block data to check
	 * 
	 * @return true if similar, false otherwise
	 */
	@SuppressWarnings("deprecation") // Draft API
	public boolean isSimilar(BlockData blockData) {
		return blockData.matches(data);
	}
	
	@Override
	public int hashCode() {
		int result = 31 * type.hashCode();
		result = 31 * result + data.hashCode();
		
		return result;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof VeinBlock)) return false;
		
		VeinBlock other = (VeinBlock) object;
		return type == other.type && data.equals(other.data);
	}
	
	@Override
	public String toString() {
		return data.getAsString();
	}
	
}