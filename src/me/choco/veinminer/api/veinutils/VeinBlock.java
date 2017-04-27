package me.choco.veinminer.api.veinutils;

import java.util.Arrays;

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
	
	private VeinTool[] mineableBy = new VeinTool[0];
	
	private final Material material;
	private final byte data;
	
	public VeinBlock(Material material, byte data){
		this.material = material;
		this.data = data;
	}
	
	public VeinBlock(Material material){
		this(material, (byte) -1);
	}
	
	/** 
	 * Get the represented Bukkit {@link Material}
	 * 
	 * @return the base material
	 */
	public Material getMaterial(){
		return material;
	}
	
	/** 
	 * Get the byte data value (if any). -1 if no data specified
	 * 
	 * @return the byte data
	 */
	public byte getData(){
		return data;
	}
	
	/** 
	 * Whether the block has specific byte data or not. Specific data
	 * will restrict the block to only be vein minable if its data value
	 * and its material are valid
	 * 
	 * @return true if data is specified (i.e. not -1)
	 */
	public boolean hasSpecficData(){
		return data != -1;
	}
	
	/**
	 * Add a tool that is capable of mining this block
	 * 
	 * @param tool - The tool to add
	 */
	public void addMineableBy(VeinTool tool) {
		if (isMineableBy(tool)) return;
		ArrayUtils.add(mineableBy, tool);
	}
	
	/**
	 * Add a list of tools that are capable of mining this block
	 * 
	 * @param tools - The tools to add
	 */
	public void addMineableBy(VeinTool... tools) {
		mineableBy = Arrays.stream(ArrayUtils.addAll(mineableBy, tools)).distinct().toArray(VeinTool[]::new);
	}
	
	/**
	 * Remove a tool from the list of tools capable of mining this block
	 * 
	 * @param tool - The tool to remove
	 */
	public void removeMineableBy(VeinTool tool) {
		ArrayUtils.removeElement(mineableBy, tool);
	}
	
	/**
	 * Check whether a tool is capable of mining this block or not
	 * 
	 * @param tool - The tool to check
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
		return mineableBy;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof VeinBlock)) return false;
		
		VeinBlock block = (VeinBlock) object;
		return (material == block.material && data == block.data);
	}
}