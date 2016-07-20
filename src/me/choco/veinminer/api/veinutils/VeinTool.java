package me.choco.veinminer.api.veinutils;

import org.bukkit.Material;

import me.choco.veinminer.VeinMiner;

/** Tools recognized by VeinMiner and it's code. Tools are limited to those listed in the enumeration
 */
public enum VeinTool {
	
	/** Represents a pickaxe of various materials. This includes:
	 * <li> Wooden Pickaxe
	 * <li> Stone Pickaxe
	 * <li> Gold Pickaxe
	 * <li> Iron Pickaxe
	 * <li> Diamond Pickaxe
	 */
	PICKAXE("Pickaxe", Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.GOLD_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE),
	
	/** Represents an axe of various materials. This includes:
	 * <li> Wooden Axe
	 * <li> Stone Axe
	 * <li> Gold Axe
	 * <li> Iron Axe
	 * <li> Diamond Axe
	 */
	AXE("Axe", Material.WOOD_AXE, Material.STONE_AXE, Material.GOLD_AXE, Material.IRON_AXE, Material.DIAMOND_AXE),
	
	/** Represents a shovel of various materials. This includes:
	 * <li> Wooden Shovel
	 * <li> Stone Shovel
	 * <li> Gold Shovel
	 * <li> Iron Shovel
	 * <li> Diamond Shovel
	 */
	SHOVEL("Shovel", Material.WOOD_SPADE, Material.STONE_SPADE, Material.GOLD_SPADE, Material.IRON_SPADE, Material.DIAMOND_SPADE),
	
	/** Represents a hoe of various materials. This includes:
	 * <li> Wooden Hoe
	 * <li> Stone Hoe
	 * <li> Gold Hoe
	 * <li> Iron Hoe
	 * <li> Diamond Hoe
	 */
	HOE("Hoe", Material.WOOD_HOE, Material.STONE_HOE, Material.GOLD_HOE, Material.IRON_HOE, Material.DIAMOND_HOE),
	
	/** Represents shears */
	SHEARS("Shears", Material.SHEARS),
	
	/** Represents all VeinTools listed in the game, i.e. pickaxe, axe, shovel, shears, etc.*/
	ALL("All");
	
	private static final VeinMiner plugin = VeinMiner.getPlugin();
	
	private final String name;
	private final Material[] materials;
	private VeinTool(String name, Material... materials) {
		this.name = name;
		this.materials = materials;
	}
	
	/** Get the name utilized in the configuration file 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/** Get all tool materials categorized under the vein tool
	 * @return all categorized tool materials
	 */
	public Material[] getMaterials() {
		return materials;
	}
	
	/** Get the maximum vein size this vein tool can break (Specified in configuration file)
	 * @return the maximum vein size
	 */
	public int getMaxVeinSize(){
		return (this != VeinTool.ALL ? plugin.getConfig().getInt("Tools." + name + ".MaxVeinSize", 64) : 64);
	}
	
	/** Get whether this vein tool will take durability whilst veinmining or not (Specified in configuration file)
	 * @return true if it takes durability damage
	 */
	public boolean usesDurability(){
		return (this != VeinTool.ALL ? plugin.getConfig().getBoolean("Tools." + name + ".UsesDurability", true) : true);
	}
	
	/** Get a VeinTool based on its name used in the configuration file. 
	 * Null if no VeinTool with the given name exists
	 * @param name - the name of the tool
	 * @return the VeinTool with the given name
	 */
	public static VeinTool getByName(String name){
		for (VeinTool tool : values())
			if (tool.getName().equalsIgnoreCase(name)) return tool;
		return null;
	}
	
	/** Get a VeinTool based on a categorized material
	 * Null if no VeinTool with the given material exists
	 * @param material - the material to search for
	 * @return the VeinTool with the given categorized material
	 */
	public static VeinTool fromMaterial(Material material){
		for (VeinTool tool : values())
			for (Material mat : tool.getMaterials())
				if (mat.equals(material)) return tool;
		return null;
	}
}