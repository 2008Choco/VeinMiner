package me.choco.veinminer.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.pattern.PatternRegistry;
import me.choco.veinminer.pattern.VeinMiningPattern;

/**
 * The central management for VeinMiner to handle everything regarding
 * veinminer and its features
 */
public class VeinMinerManager {
	
	private final List<MaterialAlias> aliases = new ArrayList<>();
	private final Set<UUID> disabledWorlds = new HashSet<>();
	private final Map<UUID, VeinMiningPattern> playerMiningPattern = new HashMap<>();
	
	private VeinMiner plugin;
	
	public VeinMinerManager(VeinMiner plugin) {
		this.plugin = plugin;
	}
	
	/** 
	 * Load all veinable blocks from the configuration file to memory
	 */
	public void loadVeinableBlocks() {
		for (String tool : plugin.getConfig().getConfigurationSection("BlockList").getKeys(false)) {
			List<String> blocks = plugin.getConfig().getStringList("BlockList." + tool);
			
			for (String value : blocks) {
				Material material = null;
				byte data = -1;
				
				String[] ids = value.split(";");
				
				//Material information
				material = Material.getMaterial(ids[0].toUpperCase());
				if (material == null) {
					this.plugin.getLogger().warning("Block id " + ids[0] + " not found! Ignoring");
					continue;
				}
				
				//Data value information
				if (ids.length > 1) {
					try{
						data = Byte.parseByte(ids[1]);
					} catch (NumberFormatException e) { 
						data = -1;
						this.plugin.getLogger().warning("Data value " + ids[1] + " could not be parsed to a byte. Assuming all data values");
					}
				}
				
				// Registration
				VeinTool veinTool = VeinTool.getByName(tool);
				if (VeinBlock.isVeinable(material, data)) {
					VeinBlock.getVeinminableBlock(material).addMineableBy(veinTool);
				} else {
					VeinBlock.registerVeinminableBlock(material, data, veinTool);
				}
			}
		}
	}
	
	/** 
	 * Load all disabled worlds from the configuration file to memory 
	 */
	public void loadDisabledWorlds() {
		this.disabledWorlds.clear();
		
		for (String worldName : plugin.getConfig().getStringList("DisabledWorlds")) {
			World world = Bukkit.getWorld(worldName);
			
			if (world == null) {
				this.plugin.getLogger().info("Unknown world found... \"" + worldName + "\". Ignoring...");
				continue;
			}
			
			this.disabledWorlds.add(world.getUID());
		}
	}
	
	/** 
	 * Check whether a world has VeinMiner disabled or not
	 * 
	 * @param world the world to check
	 * @return true if the world has VeinMiner disabled
	 */
	public boolean isDisabledInWorld(World world) {
		Preconditions.checkArgument(world != null, "Cannot check state of veinminer in null world");
		return disabledWorlds.contains(world.getUID());
	}
	
	/** 
	 * Get a list of all worlds in which VeinMiner is disabled
	 * 
	 * @return a list of all disabled worlds
	 */
	public Set<World> getDisabledWorlds() {
		return disabledWorlds.stream().map(w -> Bukkit.getWorld(w)).collect(Collectors.toSet());
	}
	
	/** 
	 * Disable vein miner in a specific world
	 * 
	 * @param world the world to disable
	 */
	public void setDisabledInWorld(World world) {
		Preconditions.checkArgument(world != null, "Cannot disable veinminer in null world");
		this.disabledWorlds.add(world.getUID());
	}
	
	/** 
	 * Enable VeinMiner in a specific world
	 * 
	 * @param world the world to disable
	 */
	public void setEnabledInWorld(World world) {
		Preconditions.checkArgument(world != null, "Cannot enable veinminer in null world");
		this.disabledWorlds.remove(world.getUID());
	}
	
	/**
	 * Clear all worlds from the blacklist
	 */
	public void clearDisabledWorlds() {
		this.disabledWorlds.clear();
	}
	
	/**
	 * Register a new MaterialAlias
	 * 
	 * @param alias the alias to register
	 */
	public void registerAlias(MaterialAlias alias) {
		Preconditions.checkArgument(alias != null, "Cannot register a null alias");
		this.aliases.add(alias);
	}
	
	/**
	 * Unregister a MaterialAlias
	 * 
	 * @param alias the alias to unregister
	 */
	public void unregisterAlias(MaterialAlias alias) {
		this.aliases.remove(alias);
	}
	
	/**
	 * Get the alias associated with a specific material and byte data
	 * 
	 * @param material the material to reference
	 * @param data the byte data to reference
	 *  
	 * @return the associated alias. null if none
	 */
	public MaterialAlias getAliasFor(Material material, byte data) {
		return this.aliases.stream()
			.filter(a -> a.isAliased(material, data))
			.findFirst().orElse(null);
	}
	
	/**
	 * Get the alias associated with a specific material
	 * 
	 * @param material the material to reference
	 * @return the associated alias. null if none
	 */
	public MaterialAlias getAliasFor(Material material) {
		return this.getAliasFor(material, (byte) -1);
	}
	
	/**
	 * Load all material aliases from config to memory
	 */
	public void loadMaterialAliases() {
		this.aliases.clear();
		
		for (String aliasList : plugin.getConfig().getStringList("Aliases")) {
			MaterialAlias alias = new MaterialAlias();
			
			for (String aliasMaterial : aliasList.split("\\s*,\\s*")) {
				Material material = null;
				byte data = -1;
				
				String[] ids = aliasMaterial.split(";");
				
				//Material information
				material = Material.getMaterial(ids[0].toUpperCase());
				if (material == null) {
					plugin.getLogger().warning("Block id " + ids[0] + " not found! Ignoring");
					continue;
				}
				
				//Data value information
				if (ids.length > 1) {
					try{
						data = Byte.parseByte(ids[1]);
					} catch (NumberFormatException e) { 
						data = -1;
						plugin.getLogger().warning("Data value " + ids[1] + " could not be parsed to a byte. Assuming all data values");
					}
				}
				
				alias.addAlias(material, data);
			}
			
			this.aliases.add(alias);
		}
	}
	
	/**
	 * Get the pattern used by the specified player. If the player is not using any specific pattern,
	 * {@link PatternRegistry#VEINMINER_PATTERN_DEFAULT} will be returned
	 * 
	 * @param player the player to get the pattern for
	 * @return the player's mining pattern
	 */
	public VeinMiningPattern getPatternFor(Player player) {
		Preconditions.checkArgument(player != null, "Cannot get the mining pattern for a null player");
		return playerMiningPattern.getOrDefault(player.getUniqueId(), PatternRegistry.VEINMINER_PATTERN_DEFAULT);
	}
	
	/**
	 * Set the pattern to use for the specified player
	 * 
	 * @param player the player whose pattern to set
	 * @param pattern the new pattern. null if default
	 */
	public void setPattern(Player player, VeinMiningPattern pattern) {
		Preconditions.checkArgument(player != null, "Cannot set the mining pattern for a null player");
		
		if (pattern == null) {
			this.playerMiningPattern.remove(player.getUniqueId());
		}
		else {
			this.playerMiningPattern.put(player.getUniqueId(), pattern);
		}
	}
	
	/**
	 * Clear all localised data in the VeinMiner Manager
	 */
	public void clearLocalisedData() {
		this.disabledWorlds.clear();
		VeinBlock.clearVeinableBlocks();
		this.playerMiningPattern.clear();
		this.aliases.clear();
		
		for (VeinTool tool : VeinTool.values())
			tool.clearPlayerInformation();
	}
	
}