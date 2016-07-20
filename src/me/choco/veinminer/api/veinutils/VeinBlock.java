package me.choco.veinminer.api.veinutils;

import org.bukkit.Material;

/** General information regarding material and byte data is stored in this class. Every
 * material listed in the configuration file creates a new VeinBlock for easy researchability
 * in the VeinMiner API
 * <br>
 * <br>Obviously this is subject to change sometime in the future when Mojang decides 
 * that magic byte numbers are not suitable for data values, however in the meantime, 
 * this class will manage that
 */
public class VeinBlock {
	
	private final Material material;
	private final byte data;
	public VeinBlock(Material material){
		this.material = material;
		this.data = -1;
	}
	
	public VeinBlock(Material material, byte data){
		this.material = material;
		this.data = data;
	}
	
	/** Get the base material for the VeinBlock
	 * @return the base material
	 */
	public Material getMaterial(){
		return material;
	}
	
	/** Get the data of the VeinBlock (if any). -1 if no data specified
	 * @return the data of the VeinBlock
	 */
	public byte getData(){
		return data;
	}
	
	/** Whether the VeinBlock has specific data or not
	 * @return true if data is specified (i.e. not -1)
	 */
	public boolean hasSpecficData(){
		return data != -1;
	}
}