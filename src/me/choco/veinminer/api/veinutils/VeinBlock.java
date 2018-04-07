package me.choco.veinminer.api.veinutils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * General information regarding material and block data. Every material listed in the configuration 
 * file will be loaded as a VeinBlock for easy accessibility in the VeinMiner API
 */
public class VeinBlock {
	
	private static final Set<VeinBlock> VEINABLE = new HashSet<>();
	
	private final Set<VeinTool> mineableBy = EnumSet.noneOf(VeinTool.class);
	
	private final Material material;
	private final BlockData data;
	
	private VeinBlock(Material material, BlockData data) {
		Preconditions.checkArgument(material != null, "Cannot add a null material alias");
		
		this.material = material;
		this.data = data;
	}
	
	private VeinBlock(Material material) {
		this(material, null);
	}
	
	/** 
	 * Get the represented Bukkit {@link Material}
	 * 
	 * @return the base material
	 */
	public Material getMaterial() {
		return material;
	}
	
	/** 
	 * Get the block data value (if any). null if no data specified
	 * 
	 * @return the block data
	 */
	public BlockData getData() {
		return data;
	}
	
	/** 
	 * Whether the block has specific block data or not. Specific data
	 * will restrict the block to only be vein minable if its data value
	 * and its material are valid
	 * 
	 * @return true if data is specified (i.e. not null)
	 */
	public boolean hasSpecficData() {
		return data != null;
	}
	
	/**
	 * Add a tool that is capable of mining this block
	 * 
	 * @param tool the tool to add
	 */
	public void addMineableBy(VeinTool tool) {
		Preconditions.checkArgument(tool != null, "Cannot add a null vein tool");
		this.mineableBy.add(tool);
	}
	
	/**
	 * Add a list of tools that are capable of mining this block
	 * 
	 * @param tools the tools to add
	 */
	public void addMineableBy(VeinTool... tools) {
		for (VeinTool tool : tools) {
			this.addMineableBy(tool);
		}
	}
	
	/**
	 * Remove a tool from the list of tools capable of mining this block
	 * 
	 * @param tool the tool to remove
	 */
	public void removeMineableBy(VeinTool tool) {
		this.mineableBy.remove(tool);
	}
	
	/**
	 * Check whether a tool is capable of mining this block or not
	 * 
	 * @param tool the tool to check
	 * @return true if it is capable of mining it
	 */
	public boolean isMineableBy(VeinTool tool) {
		return mineableBy.contains(tool);
	}
	
	/**
	 * Get all tools that are capable of mining this block
	 * 
	 * @return all tools capable of mining
	 */
	public VeinTool[] getMineableBy() {
		return mineableBy.toArray(new VeinTool[mineableBy.size()]);
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = prime + ((data == null) ? 0 : data.hashCode());
		return prime * result + ((material == null) ? 0 : material.hashCode());
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof VeinBlock)) return false;
		
		VeinBlock block = (VeinBlock) object;
		return (material == block.material && Objects.equals(data, block.data));
	}

	/** 
	 * Register a material with specific block data with all possible tools that
	 * can veinmine it
	 * 
	 * @param material the material to register
	 * @param data the block data to register
	 * @param tools the tools that are capable of mining the block
	 */
	public static void registerVeinminableBlock(Material material, BlockData data, VeinTool... tools) {
		Preconditions.checkArgument(material != null, "Cannot add a null material alias");
		if (isVeinable(material, data)) return;
		
		VeinBlock block = new VeinBlock(material, data);
		block.addMineableBy(tools);
		VEINABLE.add(block);
	}
	
	/** 
	 * Register a material with no specific data with all possible tools that 
	 * can veinmine it
	 * 
	 * @param material the material to register
	 * @param tools the tools that are capable of mining the block
	 */
	public static void registerVeinminableBlock(Material material, VeinTool... tools) {
		registerVeinminableBlock(material, null, tools);
	}
	
	/** 
	 * Unregister a specific material (with data)
	 * 
	 * @param tool the tool to unregister the material for
	 * @param material the material that should be unregistered
	 * @param data the data that should be unregistered (-1 if none)
	 */
	public static void unregisterVeinminableBlock(VeinTool tool, Material material, BlockData data) {
		Iterator<VeinBlock> it = VEINABLE.iterator();
		
		while (it.hasNext()) {
			VeinBlock block = it.next();
			if (block.material == material && (!block.hasSpecficData() || block.data.equals(data)) && block.isMineableBy(tool)) {
				it.remove();
				break;
			}
		}
	}
	
	/** 
	 * Unregister a specific material (with no specified data)
	 * 
	 * @param tool the tool to unregister the material for
	 * @param material the material that should be unregistered
	 */
	public static void unregisterVeinminableBlock(VeinTool tool, Material material) {
		unregisterVeinminableBlock(tool, material, null);
	}
	
	/**
	 * Get a registered VeinBlock instance of the specified type and data
	 * 
	 * @param material the material to search for
	 * @param data the data to search for
	 * 
	 * @return the registered vein block. null if none registered
	 */
	public static VeinBlock getVeinminableBlock(Material material, BlockData data) {
		return VEINABLE.stream()
			.filter(b -> b.material == material)
			.filter(b -> Objects.equals(b.data, data))
			.findFirst().orElse(null);
	}
	
	/**
	 * Get a registered VeinBlock instance without any specific block data
	 * 
	 * @param material the material to search for
	 * @return the registered vein block. null if none registered
	 */
	public static VeinBlock getVeinminableBlock(Material material) {
		return getVeinminableBlock(material, null);
	}
	
	/** 
	 * Whether a material (with data) is able to be broken using a specific VeinMiner tool.
	 * If a vein block with a similar material but wildcard data is veinminable, this method
	 * will return true.
	 * 
	 * @param tool the tool to check
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(VeinTool tool, Material material, BlockData data) {
		return VEINABLE.stream().anyMatch(b -> b.material == material && (!b.hasSpecficData() || b.data.equals(data)) && b.isMineableBy(tool));
	}
	
	/** 
	 * Whether a material is able to be broken using a specific VeinMiner tool
	 * 
	 * @param tool the tool to check
	 * @param material the material to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(VeinTool tool, Material material) {
		return isVeinable(tool, material, null);
	}
	
	/**
	 * Check whether a material (with data) is able to be broken using any VeinMiner tool
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
	 * Check whether a material is able to be broken using any VeinMiner tool
	 * 
	 * @param material - The material to check
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(Material material) {
		return isVeinable(null, material);
	}
	
	/** 
	 * Get a list of all blocks able to be broken by VeinMiner from a specific tool
	 * 
	 * @param tool the tool to check
	 * @return A set of all VeinMineable blocks from the tool
	 */
	public static Set<VeinBlock> getVeinminableBlocks(VeinTool tool) {
		return VEINABLE.stream()
				.filter(b -> b.isMineableBy(tool))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Clear all veinable blocks
	 */
	public static void clearVeinableBlocks() {
		VEINABLE.clear();
	}
	
}