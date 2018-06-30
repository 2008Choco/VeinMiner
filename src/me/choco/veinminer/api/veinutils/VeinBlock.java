package me.choco.veinminer.api.veinutils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Represents a set of material and block data. A VeinBlock may or may not possess additional data
 * beyond a {@link Material} type.
 */
public class VeinBlock {
	
	private static final Set<VeinBlock> VEINABLE = new HashSet<>();
	
	private final Set<VeinTool> mineableBy = EnumSet.noneOf(VeinTool.class);
	
	private final Material material;
	private final BlockData data;
	
	private VeinBlock(Material material, BlockData data) {
		Preconditions.checkNotNull(material, "Cannot add a null material alias");
		
		this.material = material;
		this.data = data;
	}
	
	private VeinBlock(Material material) {
		this(material, null);
	}
	
	/**
	 * Get the underlying Bukkit {@link Material} represented by this VeinBlock.
	 * 
	 * @return the base material
	 */
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * Get the underlying block data value (if any). null if no data specified.
	 * 
	 * @return the block data
	 */
	public BlockData getData() {
		return data;
	}
	
	/**
	 * Check whether the block has specific block data or not. In a situation where a VeinBlock
	 * has specific data is compared with that of a VeinBlock where data is not explicitly set,
	 * {@link #equals(Object)} will not yield true. For example:
	 * <p>
	 * <pre>{@code
	 * BlockData cakeData = Material.CAKE.createBlockData();
	 * cakeData.setBites(4);
	 * 
	 * VeinBlock specificDataBlock = VeinBlock.getVeinminableBlock(Material.CAKE, cakeData);
	 * VeinBlock noDataBlock = VeinBlock.getVeinminableBlock(Material.CAKE); // No data
	 * 
	 * specificDataBlock.equals(noDataBlock); // false
	 * noDataBlock.equals(specificDataBlock); // false
	 * }</pre>
	 * 
	 * @return true if data is specified (i.e. not null)
	 */
	public boolean hasSpecficData() {
		return data != null;
	}
	
	/**
	 * Add a tool that is capable of vein mining this block.
	 * 
	 * @param tool the tool to add
	 */
	public void addMineableBy(VeinTool tool) {
		Preconditions.checkNotNull(tool, "Cannot add a null vein tool");
		this.mineableBy.add(tool);
	}
	
	/**
	 * Add a list of tools that are capable of vein mining this block.
	 * 
	 * @param tools the tools to add
	 */
	public void addMineableBy(VeinTool... tools) {
		for (VeinTool tool : tools) {
			this.addMineableBy(tool);
		}
	}
	
	/**
	 * Remove a tool from the list of tools capable of vein mining this block.
	 * 
	 * @param tool the tool to remove
	 */
	public void removeMineableBy(VeinTool tool) {
		this.mineableBy.remove(tool);
	}
	
	/**
	 * Check whether a tool is capable of vein mining this block or not.
	 * 
	 * @param tool the tool to check
	 * 
	 * @return true if it is capable of mining it, false otherwise
	 */
	public boolean isMineableBy(VeinTool tool) {
		return mineableBy.contains(tool);
	}
	
	/**
	 * Get an array of tools that are capable of vein mining this block. The returned array is a
	 * copy, therefore any modifications made to the returned array will not affect this VeinBlock.
	 * 
	 * @return all tools capable of mining
	 */
	public VeinTool[] getMineableBy() {
		return mineableBy.toArray(new VeinTool[mineableBy.size()]);
	}
	
	@Override
	public int hashCode() {
		int result = 31 * ((data == null) ? 0 : data.hashCode());
		return 31 * result + ((material == null) ? 0 : material.hashCode());
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof VeinBlock)) return false;
		
		VeinBlock block = (VeinBlock) object;
		return (material == block.material && Objects.equals(data, block.data));
	}
	
	/**
	 * Register a material with specific block data with all possible tools that may vein mine it.
	 * 
	 * @param material the material to register
	 * @param data the block data to register
	 * @param tools the tools that are capable of mining the block
	 * 
	 * @return the newly created VeinBlock instance
	 */
	public static VeinBlock registerVeinminableBlock(Material material, BlockData data, VeinTool... tools) {
		Preconditions.checkNotNull(material, "Cannot add a null material alias");
		if (isVeinable(material, data)) return null;
		
		VeinBlock block = new VeinBlock(material, data);
		block.addMineableBy(tools);
		VEINABLE.add(block);
		return block;
	}
	
	/**
	 * Register a material with no specific data with all possible tools that may vein mine it.
	 * 
	 * @param material the material to register
	 * @param tools the tools that are capable of mining the block
	 * 
	 * @return the newly created VeinBlock instance
	 */
	public static VeinBlock registerVeinminableBlock(Material material, VeinTool... tools) {
		return registerVeinminableBlock(material, null, tools);
	}
	
	/**
	 * Get a registered VeinBlock instance of the specified type and data.
	 * 
	 * @param material the material to search for
	 * @param data the data to search for
	 * 
	 * @return the registered vein block. null if none registered
	 */
	public static VeinBlock getVeinminableBlock(Material material, BlockData data) {
		return VEINABLE.stream().filter(b -> b.material == material)
				.filter(b -> Objects.equals(b.data, data)).findFirst().orElse(null);
	}
	
	/**
	 * Get a registered VeinBlock instance without any specific block data.
	 * 
	 * @param material the material to search for
	 * 
	 * @return the registered vein block. null if none registered
	 */
	public static VeinBlock getVeinminableBlock(Material material) {
		return getVeinminableBlock(material, null);
	}
	
	/**
	 * Check whether a material (with data) is able to be broken using a specific VeinMiner tool. If
	 * a vein block with a similar material but wildcard data is veinminable, this method will return
	 * true.
	 * 
	 * @param tool the tool to check
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if it is breakable with VeinMiner, false otherwise
	 */
	public static boolean isVeinable(VeinTool tool, Material material, BlockData data) {
		return VEINABLE.stream().anyMatch(b -> b.material == material && (!b.hasSpecficData() || b.data.equals(data)) && b.isMineableBy(tool));
	}
	
	/**
	 * Check whether a material is able to be broken using a specific VeinMiner tool.
	 * 
	 * @param tool the tool to check
	 * @param material the material to check
	 * 
	 * @return true if it is breakable with VeinMiner, false otherwise
	 */
	public static boolean isVeinable(VeinTool tool, Material material) {
		return isVeinable(tool, material, null);
	}
	
	/**
	 * Check whether a material (with data) is able to be broken using any VeinMiner tool.
	 * 
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(Material material, BlockData data) {
		return isVeinable(null, material, data);
	}
	
	/**
	 * Check whether a material is able to be broken using any VeinMiner tool.
	 * 
	 * @param material the material to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(Material material) {
		return isVeinable(null, material);
	}
	
	/**
	 * Get a set of all blocks able to be broken by VeinMiner from a specific tool. A copy of
	 * the set is returned, therefore any changes made to the returned set will not affect this
	 * VeinBlock.
	 * 
	 * @param tool the tool to check
	 * 
	 * @return A set of all VeinMineable blocks from the tool
	 */
	public static Set<VeinBlock> getVeinminableBlocks(VeinTool tool) {
		return VEINABLE.stream().filter(b -> b.isMineableBy(tool)).collect(Collectors.toSet());
	}
	
	/**
	 * Clear all veinable blocks.
	 */
	public static void clearVeinableBlocks() {
		VEINABLE.clear();
	}
	
}