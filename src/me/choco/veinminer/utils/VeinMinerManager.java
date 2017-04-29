package me.choco.veinminer.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;

/**
 * The central management for VeinMiner to handle everything regarding
 * veinminer and its features
 */
public class VeinMinerManager {
	
	private final List<MaterialAlias> aliases = new ArrayList<>();
	
	private final Set<UUID> disabledWorlds = new HashSet<>();
	private final Set<VeinBlock> veinable = new HashSet<>();
	
	private VeinMiner plugin;
	
	public VeinMinerManager(VeinMiner plugin) {
		this.plugin = plugin;
	}
	
	/** 
	 * Load all veinable blocks from the configuration file to memory
	 */
	public void loadVeinableBlocks(){
		this.veinable.clear();
		
		for (String tool : plugin.getConfig().getConfigurationSection("BlockList").getKeys(false)){
			List<String> blocks = plugin.getConfig().getStringList("BlockList." + tool);
			
			for (String value : blocks){
				Material material = null;
				byte data = -1;
				
				String[] ids = value.split(";");
				
				//Material information
				material = Material.getMaterial(ids[0].toUpperCase());
				if (material == null){
					plugin.getLogger().warning("Block id " + ids[0] + " not found! Ignoring");
					continue;
				}
				
				//Data value information
				if (ids.length > 1){
					try{
						data = Byte.parseByte(ids[1]);
					}catch(NumberFormatException e){ 
						data = -1;
						plugin.getLogger().warning("Data value " + ids[1] + " could not be parsed to a byte. Assuming all data values");
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
	 * Get a set of all UUID's that currently have VeinMiner disabled
	 * 
	 * @param tool - The tool to get disabled players for
	 * @return a set of all disabled UUID's
	 * 
	 * @deprecated See {@link VeinTool#getDisabledBy()}
	 */
	@Deprecated
	public Set<OfflinePlayer> getPlayersWithVeinMinerDisabled(VeinTool tool){
		return tool.getDisabledBy();
	}
	
	/**
	 * Check whether a player has VeinMiner currently disabled or not
	 * 
	 * @param player - The player to check
	 * @param tool - The tool to check disabled players for
	 * 
	 * @return true if VeinMiner is disabled for the player
	 * @deprecated See {@link VeinTool#hasVeinMinerDisabled(OfflinePlayer)}
	 */
	@Deprecated
	public boolean hasVeinMinerDisabled(OfflinePlayer player, VeinTool tool){
		return tool.hasVeinMinerDisabled(player);
	}
	
	/** 
	 * Check whether a UUID has VeinMiner currently disabled or not
	 * 
	 * @param uuid - The UUID to check
	 * @param tool - The tool to check disabled UUID's for
	 * 
	 * @return true if VeinMiner is disabled for the UUID
	 * @deprecated See {@link VeinTool#hasVeinMinerDisabled(OfflinePlayer)}
	 */
	@Deprecated
	public boolean hasVeinMinerDisabled(UUID uuid, VeinTool tool){
		return tool.hasVeinMinerDisabled(Bukkit.getOfflinePlayer(uuid));
	}
	
	/** 
	 * Check whether a player has VeinMiner currently enabled or not
	 * 
	 * @param player - The player to check
	 * @param tool - The tool to check enabled players for
	 * 
	 * @return true if VeinMiner is enabled for the player
	 * @deprecated See {@link VeinTool#hasVeinMinerEnabled(OfflinePlayer)}
	 */
	@Deprecated
	public boolean hasVeinMinerEnabled(OfflinePlayer player, VeinTool tool){
		return tool.hasVeinMinerEnabled(player);
	}
	
	/** 
	 * Check whether a UUID has VeinMiner currently enabled or not
	 * 
	 * @param uuid - The UUID to check
	 * @param tool - The tool to check enabled UUID's for
	 * 
	 * @return true if VeinMiner is enabled for the UUID
	 * @deprecated See {@link VeinTool#hasVeinMinerEnabled(OfflinePlayer)}
	 */
	@Deprecated
	public boolean hasVeinMinerEnabled(UUID uuid, VeinTool tool){
		return tool.hasVeinMinerEnabled(Bukkit.getOfflinePlayer(uuid));
	}
	
	/** 
	 * Toggle whether VeinMiner is enabled or disabled for a specific player
	 * 
	 * @param player - The player to toggle
	 * @param tool - The tool in which should be toggled
	 * 
	 * @deprecated See {@link VeinTool#toggleVeinMiner(OfflinePlayer)}
	 */
	@Deprecated
	public void toggleVeinMiner(OfflinePlayer player, VeinTool tool){
		tool.toggleVeinMiner(player);
	}
	
	/** 
	 * Toggle whether VeinMiner is enabled or disabled for a specific UUID
	 * 
	 * @param uuid - The UUID to toggle
	 * @param tool - The tool in which should be toggled
	 * 
	 * @deprecated See {@link VeinTool#toggleVeinMiner(OfflinePlayer)}
	 */
	@Deprecated
	public void toggleVeinMiner(UUID uuid, VeinTool tool){
		tool.toggleVeinMiner(Bukkit.getOfflinePlayer(uuid));
	}
	
	/** 
	 * Set whether a players VeinMiner should be enabled or not
	 * 
	 * @param player - The player to toggle
	 * @param tool - The tool to affect
	 * @param toggle - Whether it should be enabled (true) or disabled (false)
	 * 
	 * @deprecated See {@link VeinTool#toggleVeinMiner(OfflinePlayer)}
	 */
	@Deprecated
	public void toggleVeinMiner(OfflinePlayer player, VeinTool tool, boolean toggle){
		tool.toggleVeinMiner(player, toggle);
	}
	
	/** 
	 * Set whether a UUID's VeinMiner should be enabled or not
	 * 
	 * @param uuid - The UUID to toggle
	 * @param tool - The tool to affect
	 * @param toggle - Whether it should be enabled (true) or disabled (false)
	 * 
	 * @deprecated See {@link VeinTool#toggleVeinMiner(OfflinePlayer)}
	 */
	@Deprecated
	public void toggleVeinMiner(UUID uuid, VeinTool tool, boolean toggle){
		tool.toggleVeinMiner(Bukkit.getOfflinePlayer(uuid), toggle);
	}
	
	/** 
	 * Load all disabled worlds from the configuration file to memory 
	 */
	public void loadDisabledWorlds(){
		disabledWorlds.clear();
		for (String worldName : plugin.getConfig().getStringList("DisabledWorlds")){
			World world = Bukkit.getWorld(worldName);
			
			if (world == null){
				plugin.getLogger().info("Unknown world found... \"" + worldName + "\". Ignoring...");
				continue;
			}
			
			disabledWorlds.add(world.getUID());
		}
	}
	
	/** 
	 * Check whether a world has VeinMiner disabled or not
	 * 
	 * @param world - The world to check
	 * @return true if the world has VeinMiner disabled
	 */
	public boolean isDisabledInWorld(World world){
		return disabledWorlds.contains(world.getUID());
	}
	
	/** 
	 * Get a list of all worlds in which VeinMiner is disabled
	 * 
	 * @return a list of all disabled worlds
	 */
	public Set<World> getDisabledWorlds(){
		return disabledWorlds.stream().map(w -> Bukkit.getWorld(w)).collect(Collectors.toSet());
	}
	
	/** 
	 * Disable vein miner in a specific world
	 * 
	 * @param world - The world to disable
	 */
	public void setDisabledInWorld(World world){
		if (!isDisabledInWorld(world)) disabledWorlds.add(world.getUID());
	}
	
	/** 
	 * Enable VeinMiner in a specific world
	 * 
	 * @param world - The world to disable
	 */
	public void setEnabledInWorld(World world){
		if (isDisabledInWorld(world)) disabledWorlds.remove(world.getUID());
	}
	
	/**
	 * Register a new MaterialAlias
	 * 
	 * @param alias - The alias to register
	 */
	public void registerAlias(MaterialAlias alias) {
		this.aliases.add(alias);
	}
	
	/**
	 * Unregister a MaterialAlias
	 * 
	 * @param alias - The alias to unregister
	 */
	public void unregisterAlias(MaterialAlias alias) {
		this.aliases.remove(alias);
	}
	
	/**
	 * Get the alias associated with a specific material and byte data
	 * 
	 * @param material - The material to reference
	 * @param data - The byte data to reference
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
	 * @param material - The material to reference
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
			
			for (String aliasMaterial : aliasList.split("\\s,\\s")) {
				Material material = null;
				byte data = -1;
				
				String[] ids = aliasMaterial.split(";");
				
				//Material information
				material = Material.getMaterial(ids[0].toUpperCase());
				if (material == null){
					plugin.getLogger().warning("Block id " + ids[0] + " not found! Ignoring");
					continue;
				}
				
				//Data value information
				if (ids.length > 1){
					try{
						data = Byte.parseByte(ids[1]);
					}catch(NumberFormatException e){ 
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
	 * Clear all localised data in the VeinMiner Manager
	 */
	public void clearLocalisedData() {
		this.disabledWorlds.clear();
		this.veinable.clear();
		this.aliases.clear();
		
		for (VeinTool tool : VeinTool.values())
			tool.clearPlayerInformation();
	}
}