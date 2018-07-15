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
 * beyond a {@link Material} type; as such, VeinBlock may be an instance of either {@link VeinBlockDatable}
 * or {@link VeinBlockWildcarded}.
 * 
 * <ul>
 *   <li><strong>VeinBlockDatable:</strong> Has specific BlockData. {@link #isWildcarded()} == true and
 *   {@link #getData()} != null.
 *   <li><strong>VeinBlockWildcarded:</strong> Has no specific BlockData and encompasses all blocks of
 *   the provided type.
 * </ul>
 */
public abstract class VeinBlock {
	
	private final Set<VeinTool> tools = EnumSet.noneOf(VeinTool.class);
	protected final Material type;
	
	protected VeinBlock(Material type) {
		this.type = type;
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
	 * Get the specific data associated with this block, if any. Depending on the implementation of VeinBlock
	 * at hand, this method may return null, therefore precautions should be taken. The result of
	 * {@link #isWildcarded()} should first be checked before safely invoking methods on this object.
	 * 
	 * @return the associated data, if any. Can be null
	 */
	public abstract BlockData getData();
	
	/**
	 * Check whether this VeinBlock has wildcarded data or not. If true, specific block data is taken into
	 * consideration when checking similarity. If false, only type is checked and the result of {@link #getData()}
	 * will always be null.
	 * 
	 * @return true if wildcarded, false otherwise
	 * 
	 * @see VeinBlockWildcarded
	 */
	public abstract boolean isWildcarded();
	
	/**
	 * Check whether the provided block is encapsulated by this VeinBlock or not. If this is an instance of
	 * {@link VeinBlockWildcarded}, regardless of the block's specific data, this method will return true
	 * such that the block's type is identical to that returned by {@link #getType()}. If this is an instance
	 * of {@link VeinBlockDatable}, both the block's type and data must equal that of the VeinBlock.
	 * 
	 * @param block the block to check
	 * 
	 * @return true if similar, false otherwise
	 */
	public abstract boolean isSimilar(Block block);
	
	/**
	 * Check whether the provided block data is encapsulated by this VeinBlock or not. If this is an instance
	 * of {@link VeinBlockWildcarded}, regardless of the block data at hand, this method will return true such
	 * that the data's type is identical to that returned by {@link #getType()}. If this is an instance of
	 * {@link VeinBlockDatable}, both the block data's type and data must equal that of the VeinBlock.
	 * 
	 * @param blockData the block data to check
	 * 
	 * @return true if similar, false otherwise
	 */
	public abstract boolean isSimilar(BlockData blockData);
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object object);
	
	@Override
	public abstract String toString();
	
}