package wtf.choco.veinminer.tool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import wtf.choco.veinminer.VeinMiner;

/**
 * Tool categories recognised by VeinMiner and it's code. Tool materials are limited
 * to those listed in the enumeration.
 */
public enum ToolCategory {

	/**
	 * Represents a pickaxe of various materials. This includes:
	 * <ul>
	 *   <li>Wooden Pickaxe
	 *   <li>Stone Pickaxe
	 *   <li>Golden Pickaxe
	 *   <li>Iron Pickaxe
	 *   <li>Diamond Pickaxe
	 * </ul>
	 */
	PICKAXE("Pickaxe", Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE),

	/**
	 * Represents an axe of various materials. This includes:
	 * <ul>
	 *   <li>Wooden Axe
	 *   <li>Stone Axe
	 *   <li>Golden Axe
	 *   <li>Iron Axe
	 *   <li>Diamond Axe
	 * </ul>
	 */
	AXE("Axe", Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE),

	/**
	 * Represents a shovel of various materials. This includes:
	 * <ul>
	 *   <li>Wooden Shovel
	 *   <li>Stone Shovel
	 *   <li>Golden Shovel
	 *   <li>Iron Shovel
	 *   <li>Diamond Shovel
	 * </ul>
	 */
	SHOVEL("Shovel", Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL),

	/**
	 * Represents a hoe of various materials. This includes:
	 * <ul>
	 *   <li>Wooden Hoe
	 *   <li>Stone Hoe
	 *   <li>Golden Hoe
	 *   <li>Iron Hoe
	 *   <li>Diamond Hoe
	 * </ul>
	 */
	HOE("Hoe", Material.WOODEN_HOE, Material.STONE_HOE, Material.GOLDEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE),

	/**
	 * Represents shears
	 */
	SHEARS("Shears", Material.SHEARS),

	/**
	 * Represent's a player's hands; i.e. no tool at all
	 */
	HAND("Hand");


	private static final VeinMiner plugin = VeinMiner.getPlugin();

	private final Set<UUID> disabledBy = new HashSet<>();

	private final String name;
	private final Material[] materials;

	private ToolCategory(String name, Material... materials) {
		this.name = name;
		this.materials = materials;
	}

	/**
	 * Get the name for this tool used in the configuration file.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get all Materials associated with this VeinTool.
	 *
	 * @return all associated tool materials
	 */
	public Material[] getMaterials() {
		return Arrays.copyOf(materials, materials.length);
	}

	/**
	 * Get the maximum vein size this VeinTool is capable of breaking.This option is specified in and
	 * directly retrieved from the configuration file.
	 *
	 * @return the maximum vein size. Defaults to 64 if not explicitly set
	 */
	public int getMaxVeinSize() {
		return plugin.getConfig().getInt("Tools." + name + ".MaxVeinSize", 64);
	}

	/**
	 * Get a VeinTool based on its name used in the configuration file. null if no VeinTool with the
	 * given name exists.
	 *
	 * @param name the name of the tool. Case insensitive
	 *
	 * @return the VeinTool with the given name. null if none
	 */
	public static ToolCategory getByName(String name) {
		for (ToolCategory tool : values())
			if (tool.getName().equalsIgnoreCase(name)) return tool;
		return null;
	}

	/**
	 * Get the VeinTool associated with the specified material. If none exist, {@link #HAND} is
	 * returned.
	 *
	 * @param material the material for which to search
	 *
	 * @return the VeinTool associated with the specified material. {@link #HAND} if none
	 */
	public static ToolCategory fromMaterial(Material material) {
		for (ToolCategory tool : values()) {
			for (Material toolMaterial : tool.getMaterials()) {
				if (toolMaterial == material) return tool;
			}
		}

		return HAND;
	}

	/**
	 * Disable VeinMiner for this tool for a specific player.
	 *
	 * @param player the player for whom to disable the tool
	 */
	public void disableVeinMiner(OfflinePlayer player) {
		Preconditions.checkNotNull(player, "Cannot disable veinminer for a null player");
		this.disabledBy.add(player.getUniqueId());
	}

	/**
	 * Enable VeinMiner for this tool for a specific player.
	 *
	 * @param player the player for whom to disable the tool
	 */
	public void enableVeinMiner(OfflinePlayer player) {
		Preconditions.checkNotNull(player, "Cannot enable veinminer for a null player");
		this.disabledBy.remove(player.getUniqueId());
	}

	/**
	 * Check whether this vein tool is disabled for a specific player.
	 *
	 * @param player the player to check
	 *
	 * @return true if VeinMiner is disabled
	 */
	public boolean hasVeinMinerDisabled(OfflinePlayer player) {
		Preconditions.checkNotNull(player, "Cannot check veinminer state for a null player");
		return disabledBy.contains(player.getUniqueId());
	}

	/**
	 * Check whether this vein tool is enabled for a specific player.
	 *
	 * @param player the player to check
	 *
	 * @return true if VeinMiner is enabled
	 */
	public boolean hasVeinMinerEnabled(OfflinePlayer player) {
		return !hasVeinMinerDisabled(player);
	}

	/**
	 * Toggle whether this tool is enabled or not for a specific player.
	 *
	 * @param player the player for whom to toggle this tool
	 */
	public void toggleVeinMiner(OfflinePlayer player) {
		this.toggleVeinMiner(player, !hasVeinMinerEnabled(player));
	}

	/**
	 * Toggle whether this tool is enabled or not for a specific player.
	 *
	 * @param player the player for whom to toggle this tool
	 * @param enabled the new enable state. true to enable, false otherwise
	 */
	public void toggleVeinMiner(OfflinePlayer player, boolean enabled) {
		Preconditions.checkNotNull(player, "Cannot toggle veinminer for a null player");

		if (hasVeinMinerDisabled(player) && enabled) {
			this.disabledBy.remove(player.getUniqueId());
		} else if (hasVeinMinerEnabled(player) && !enabled) {
			this.disabledBy.add(player.getUniqueId());
		}
	}

	/**
	 * Get a set of all players that have this tool disabled.
	 *
	 * @return all players disabling this tool
	 */
	public Set<OfflinePlayer> getDisabledBy() {
		return disabledBy.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
	}

	/**
	 * Clear all information regarding players that have VeinMiner disabled.
	 */
	public void clearPlayerInformation() {
		this.disabledBy.clear();
	}

}