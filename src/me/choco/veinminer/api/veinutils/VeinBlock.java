package me.choco.veinminer.api.veinutils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;

/**
 * General information regarding material and byte data. Every material listed in the configuration 
 * file will be loaded as a VeinBlock for easy accessibility in the VeinMiner API
 * <p>
 * <b>NOTE:</b> This is subject to change by Minecraft 1.13 where byte data values are no longer suitable
 * to represent block states
 */
public class VeinBlock {
	
	private static Set<VeinBlock> veinable = new HashSet<>();
	
	private VeinTool[] mineableBy = new VeinTool[0];
	
	private final Material material;
	private final byte data;
	
	private VeinBlock(Material material, byte data) {
		Preconditions.checkArgument(material != null, "Cannot add a null material alias");
		Preconditions.checkArgument(data >= -1, "Data values lower than 0 are unsupported (excluding the wildcard, -1)");
		
		this.material = material;
		this.data = data;
	}
	
	private VeinBlock(Material material) {
		this(material, (byte) -1);
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
	 * Get the byte data value (if any). -1 if no data specified
	 * 
	 * @return the byte data
	 */
	public byte getData() {
		return data;
	}
	
	/** 
	 * Whether the block has specific byte data or not. Specific data
	 * will restrict the block to only be vein minable if its data value
	 * and its material are valid
	 * 
	 * @return true if data is specified (i.e. not -1)
	 */
	public boolean hasSpecficData() {
		return data != -1;
	}
	
	/**
	 * Add a tool that is capable of mining this block
	 * 
	 * @param tool the tool to add
	 */
	public void addMineableBy(VeinTool tool) {
		Preconditions.checkArgument(tool != null, "Cannot add a null vein tool");
		if (isMineableBy(tool)) return;
		
		this.mineableBy = ArrayUtils.add(mineableBy, tool);
	}
	
	/**
	 * Add a list of tools that are capable of mining this block
	 * 
	 * @param tools the tools to add
	 */
	public void addMineableBy(VeinTool... tools) {
		this.mineableBy = Arrays.stream(ArrayUtils.addAll(mineableBy, tools)).filter(t -> t != null).distinct().toArray(VeinTool[]::new);
	}
	
	/**
	 * Remove a tool from the list of tools capable of mining this block
	 * 
	 * @param tool the tool to remove
	 */
	public void removeMineableBy(VeinTool tool) {
		this.mineableBy = ArrayUtils.removeElement(mineableBy, tool);
	}
	
	/**
	 * Check whether a tool is capable of mining this block or not
	 * 
	 * @param tool the tool to check
	 * @return true if it is capable of mining it
	 */
	public boolean isMineableBy(VeinTool tool) {
		return ArrayUtils.contains(mineableBy, tool);
	}
	
	/**
	 * Get all tools that are capable of mining this block
	 * 
	 * @return all tools capable of mining
	 */
	public VeinTool[] getMineableBy() {
		return Arrays.copyOf(mineableBy, mineableBy.length);
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = prime + data;
		return prime * result + ((material == null) ? 0 : material.hashCode());
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof VeinBlock)) return false;
		
		VeinBlock block = (VeinBlock) object;
		return (material == block.material && data == block.data);
	}

	/** 
	 * Register a material with specific byte data with all possible tools that
	 * can veinmine it
	 * 
	 * @param material the material to register
	 * @param data the byte data to register
	 * @param tools the tools that are capable of mining the block
	 */
	public static void registerVeinminableBlock(Material material, byte data, VeinTool... tools) {
		Preconditions.checkArgument(material != null, "Cannot add a null material alias");
		Preconditions.checkArgument(data >= -1, "Data values lower than 0 are unsupported (excluding the wildcard, -1)");
		if (isVeinable(material, data)) return;
		
		VeinBlock block = new VeinBlock(material, data);
		block.addMineableBy(tools);
		veinable.add(block);
	}
	
	/** 
	 * Register a material with no specific data with all possible tools that 
	 * can veinmine it
	 * 
	 * @param material the material to register
	 * @param tools the tools that are capable of mining the block
	 */
	public static void registerVeinminableBlock(Material material, VeinTool... tools) {
		registerVeinminableBlock(material, (byte) -1, tools);
	}
	
	/** 
	 * Unregister a specific material (with data)
	 * 
	 * @param tool the tool to unregister the material for
	 * @param material the material that should be unregistered
	 * @param data the data that should be unregistered (-1 if none)
	 */
	public static void unregisterVeinminableBlock(VeinTool tool, Material material, byte data) {
		Iterator<VeinBlock> it = veinable.iterator();
		
		while (it.hasNext()) {
			VeinBlock block = it.next();
			if (block.material == material && (!block.hasSpecficData() || block.data == data) && block.isMineableBy(tool)) {
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
		unregisterVeinminableBlock(tool, material, (byte) -1);
	}
	
	/**
	 * Get a registered VeinBlock instance of the specified type and data. If one
	 * does not exist, it will be created and added to the veinable list
	 * 
	 * @param material the material to search for
	 * @param data the data to search for
	 * 
	 * @return the registered vein block. null if none registered
	 */
	public static VeinBlock getVeinminableBlock(Material material, byte data) {
		return veinable.stream()
			.filter(b -> b.material == material)
			.filter(b -> b.data == data)
			.findFirst()
			.orElseGet(() -> {
				VeinBlock block = new VeinBlock(material, data);
				veinable.add(block);
				return block;
			});
	}
	
	/**
	 * Get a registered VeinBlock instance without any specific byte data
	 * 
	 * @param material the material to search for
	 * @return the registered vein block. null if none registered
	 */
	public static VeinBlock getVeinminableBlock(Material material) {
		return getVeinminableBlock(material, (byte) -1);
	}
	
	/** 
	 * Whether a material (with data) is able to be broken using a specific VeinMiner tool
	 * 
	 * @param tool the tool to check
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(VeinTool tool, Material material, byte data) {
		return veinable.stream().anyMatch(b -> b.material == material && (!b.hasSpecficData() || b.data == data) && b.isMineableBy(tool));
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
		return isVeinable(tool, material, (byte) -1);
	}
	
	/**
	 * Check whether a material (with data) is able to be broken using any VeinMiner tool
	 * 
	 * @param material the material to check
	 * @param data the data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public static boolean isVeinable(Material material, byte data) {
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
		return veinable.stream()
				.filter(b -> b.isMineableBy(tool))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Clear all veinable blocks
	 */
	public static void clearVeinableBlocks() {
		veinable.clear();
	}
	
}