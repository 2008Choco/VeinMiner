package me.choco.veinminer.api.veinutils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import me.choco.veinminer.VeinMiner;

/**
 * Tools recognised by VeinMiner and it's code. Tools are limited to those listed in the enumeration
 */
public enum VeinTool {
	
	/**
	 * Represents a pickaxe of various materials. This includes:
	 * <br> - Wooden Pickaxe
	 * <br> - Stone Pickaxe
	 * <br> - Gold Pickaxe
	 * <br> - Iron Pickaxe
	 * <br> - Diamond Pickaxe
	 */
	PICKAXE("Pickaxe", Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.GOLD_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE),
	
	/**
	 * Represents an axe of various materials. This includes:
	 * <br> - Wooden Axe
	 * <br> - Stone Axe
	 * <br> - Gold Axe
	 * <br> - Iron Axe
	 * <br> - Diamond Axe
	 */
	AXE("Axe", Material.WOOD_AXE, Material.STONE_AXE, Material.GOLD_AXE, Material.IRON_AXE, Material.DIAMOND_AXE),
	
	/**
	 * Represents a shovel of various materials. This includes:
	 * <br> - Wooden Shovel
	 * <br> - Stone Shovel
	 * <br> - Gold Shovel
	 * <br> - Iron Shovel
	 * <br> - Diamond Shovel
	 */
	SHOVEL("Shovel", Material.WOOD_SPADE, Material.STONE_SPADE, Material.GOLD_SPADE, Material.IRON_SPADE, Material.DIAMOND_SPADE),
	
	/**
	 * Represents a hoe of various materials. This includes:
	 * <br> - Wooden Hoe
	 * <br> - Stone Hoe
	 * <br> - Gold Hoe
	 * <br> - Iron Hoe
	 * <br> - Diamond Hoe
	 */
	HOE("Hoe", Material.WOOD_HOE, Material.STONE_HOE, Material.GOLD_HOE, Material.IRON_HOE, Material.DIAMOND_HOE),
	
	/**
	 * Represents shears
	 */
	SHEARS("Shears", Material.SHEARS),
	
	/**
	 * Represents all VeinTools listed in the game, i.e. pickaxe, axe, shovel, shears, etc.
	 */
	ALL("All");
	
	private static final VeinMiner plugin = VeinMiner.getPlugin();
	
	private final Set<UUID> disabledBy = new HashSet<>();
	
	private final String name;
	private final Material[] materials;
	
	private VeinTool(String name, Material... materials) {
		this.name = name;
		this.materials = materials;
	}
	
	/**
	 * Get the name provided in the configuration file
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get all tool materials categorised under the vein tool
	 * 
	 * @return all categorised tool materials
	 */
	public Material[] getMaterials() {
		return materials;
	}
	
	/**
	 * Get the maximum vein size this vein tool can break (Specified in configuration file)
	 * 
	 * @return the maximum vein size
	 */
	public int getMaxVeinSize() {
		return (this != VeinTool.ALL ? plugin.getConfig().getInt("Tools." + name + ".MaxVeinSize", 64) : 64);
	}
	
	/**
	 * Get whether this vein tool will take durability whilst veinmining or not (Specified in
	 * configuration file)
	 * 
	 * @return true if it takes durability damage
	 */
	public boolean usesDurability() {
		return (this != VeinTool.ALL ? plugin.getConfig().getBoolean("Tools." + name + ".UsesDurability", true) : true);
	}
	
	/**
	 * Get a VeinTool based on its name used in the configuration file. Null if no VeinTool with the
	 * given name exists
	 * 
	 * @param name the name of the tool
	 * @return the VeinTool with the given name
	 */
	public static VeinTool getByName(String name) {
		for (VeinTool tool : values())
			if (tool.getName().equalsIgnoreCase(name)) return tool;
		return null;
	}
	
	/**
	 * Get a VeinTool based on a categorised material. null if no VeinTool with the given 
	 * material exists
	 * 
	 * @param material the material to search for
	 * @return the VeinTool with the given categorised material
	 */
	public static VeinTool fromMaterial(Material material) {
		for (VeinTool tool : values())
			for (Material mat : tool.getMaterials())
				if (mat.equals(material)) return tool;
		return null;
	}
	
	/**
	 * Disable VeinMiner for this tool for a specific player
	 * 
	 * @param player the player to disable it for
	 */
	public void disableVeinMiner(OfflinePlayer player) {
		Preconditions.checkArgument(player != null, "Cannot disable veinminer for a null player");
		this.disabledBy.add(player.getUniqueId());
	}
	
	/**
	 * Enable VeinMiner for this tool for a specific player
	 * 
	 * @param player the player to enable it for
	 */
	public void enableVeinMiner(OfflinePlayer player) {
		Preconditions.checkArgument(player != null, "Cannot enable veinminer for a null player");
		this.disabledBy.remove(player.getUniqueId());
	}
	
	/**
	 * Check whether this vein tool is disabled for a specific player
	 * 
	 * @param player the player to check
	 * @return true if veinminer disabled
	 */
	public boolean hasVeinMinerDisabled(OfflinePlayer player) {
		Preconditions.checkArgument(player != null, "Cannot check veinminer state for a null player");
		return this.disabledBy.contains(player.getUniqueId());
	}
	
	/**
	 * Check whether this vein tool is enabled for a specific player
	 * 
	 * @param player the player to check
	 * @return true if veinminer enabled
	 */
	public boolean hasVeinMinerEnabled(OfflinePlayer player) {
		return !this.hasVeinMinerDisabled(player);
	}
	
	/**
	 * Toggle the enable state for this tool for a specific player
	 * 
	 * @param player the player to toggle this tool for
	 */
	public void toggleVeinMiner(OfflinePlayer player) {
		Preconditions.checkArgument(player != null, "Cannot toggle veinminer for a null player");
		
		if (hasVeinMinerEnabled(player)) {
			this.disabledBy.add(player.getUniqueId());
		}
		else {
			this.disabledBy.remove(player.getUniqueId());
		}
	}
	
	/**
	 * Toggle the enable state for this tool for a specific player
	 * 
	 * @param player the player to toggle this tool for
	 * @param enabled the new enable state
	 */
	public void toggleVeinMiner(OfflinePlayer player, boolean enabled) {
		Preconditions.checkArgument(player != null, "Cannot toggle veinminer for a null player");
		
		if (hasVeinMinerDisabled(player) && enabled) {
			this.disabledBy.remove(player.getUniqueId());
		}
		else if (hasVeinMinerEnabled(player) && !enabled) {
			this.disabledBy.add(player.getUniqueId());
		}
	}
	
	/**
	 * Get a list of all players that have this tool disabled
	 * 
	 * @return all players disabling this tool
	 */
	public Set<OfflinePlayer> getDisabledBy() {
		return this.disabledBy.stream().map(p -> Bukkit.getOfflinePlayer(p)).collect(Collectors.toSet());
	}
	
	/**
	 * Clear all information regarding players that have VeinMiner disabled
	 */
	public void clearPlayerInformation() {
		this.disabledBy.clear();
	}
}