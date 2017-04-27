package me.choco.veinminer.api.veinutils;

import org.bukkit.Material;

/**
 * General information regarding material and byte data. Every material listed in the configuration 
 * file will be loaded as a VeinBlock for easy accessibility in the VeinMiner API
 * <p>
 * <b>NOTE:</b> This is subject to change by Minecraft 1.13 where byte data values are no longer suitable
 * to represent block states
 */
public class VeinBlock {
	
	private final Material material;
	private final byte data;
	
	public VeinBlock(Material material, byte data){
		this.material = material;
		this.data = data;
	}
	
	public VeinBlock(Material material){
		this.material = material;
		this.data = -1;
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
}