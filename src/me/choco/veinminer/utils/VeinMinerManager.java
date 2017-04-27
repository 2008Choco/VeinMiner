package me.choco.veinminer.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;

/**
 * The central management for VeinMiner to handle everything regarding
 * veinminer and its features
 */
public class VeinMinerManager {
	
	private Set<UUID> disabledWorlds = new HashSet<>();
	private Set<VeinBlock> veinable = new HashSet<>();
	
	private VeinMiner plugin;
	
	public VeinMinerManager(VeinMiner plugin) {
		this.plugin = plugin;
	}
	
	/** 
	 * Register a VeinBlock with all possible tools that can veinmine it
	 * 
	 * @param block - The block to register
	 * @param tools - The tools that are capable of mining the block
	 */
	public void registerVeinminableBlock(VeinBlock block, VeinTool... tools) {
		if (this.veinable.contains(block)) return;
		
		block.addMineableBy(tools);
		this.veinable.add(block);
	}
	
	/** 
	 * Unregister a specific material (with data)
	 * 
	 * @param material - The material that should be unregistered
	 * @param data - The data that should be unregistered (-1 if none)
	 */
	public void unregisterVeinminableBlock(VeinTool tool, Material material, byte data){
		Iterator<VeinBlock> it = veinable.iterator();
		while (it.hasNext()){
			VeinBlock block = it.next();
			if (block.getMaterial() == material && (!block.hasSpecficData() || data == -1 || block.getData() == data)
					&& block.isMineableBy(tool)){
				it.remove();
				break;
			}
		}
	}
	
	/** 
	 * Unregister a specific material (with no specified data)
	 * 
	 * @param tool - The tool to unregister the tool for
	 * @param material - The material that should be unregistered
	 */
	public void unregisterVeinminableBlock(VeinTool tool, Material material){
		this.unregisterVeinminableBlock(tool, material, (byte) -1);
	}
	
	/**
	 * Get a registered VeinBlock instance of the specified type and data
	 * 
	 * @param material - The material to search for
	 * @param data - The data to search for
	 * 
	 * @return the registered vein block. null if none registered
	 */
	public VeinBlock getVeinminableBlock(Material material, byte data) {
		return this.veinable.stream()
			.filter(b -> b.getMaterial() == material)
			.filter(b -> (!b.hasSpecficData() || data == -1 || b.getData() == data))
			.findFirst().orElse(null);
	}
	
	/**
	 * Get a registered VeinBlock instance without any specific byte data
	 * 
	 * @param material - The material to search for
	 * @return the registered vein block. null if none registered
	 */
	public VeinBlock getVeinminableBlock(Material material) {
		return this.getVeinminableBlock(material, (byte) -1);
	}
	
	/** 
	 * Whether a material (with data) is able to be broken using a specific VeinMiner tool
	 * 
	 * @param tool - The tool to check
	 * @param material - The material to check
	 * @param data - The data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public boolean isVeinable(VeinTool tool, Material material, byte data) {
		VeinBlock block = this.getVeinminableBlock(material, data);
		return block != null && block.isMineableBy(tool);
	}
	
	/** 
	 * Whether a material is able to be broken using a specific VeinMiner tool
	 * 
	 * @param tool - The tool to check
	 * @param material - The material to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public boolean isVeinable(VeinTool tool, Material material){
		return this.isVeinable(tool, material, (byte) -1);
	}
	
	/**
	 * Check whether a material (with data) is able to be broken using any VeinMiner tool
	 * 
	 * @param material - The material to check
	 * @param data - The data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public boolean isVeinable(Material material, byte data) {
		return this.isVeinable(null, material, data);
	}
	
	/**
	 * Check whether a material is able to be broken using any VeinMiner tool
	 * 
	 * @param material - The material to check
	 * @return true if it is breakable with VeinMiner
	 */
	public boolean isVeinable(Material material) {
		return this.isVeinable(null, material);
	}
	
	/** 
	 * Get a list of all blocks able to be broken by VeinMiner from a specific tool
	 * 
	 * @param tool - The tool to check
	 * @return A set of all VeinMineable blocks from the tool
	 */
	public Set<VeinBlock> getVeinminableBlocks(VeinTool tool){
		return this.veinable.stream()
				.filter(b -> b.isMineableBy(tool))
				.collect(Collectors.toSet());
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
				if (value.contains(";")){
					try{
						data = Byte.parseByte(ids[1]);
					}catch(NumberFormatException e){ 
						data = -1;
						plugin.getLogger().warning("Data value " + ids[1] + " could not be parsed to a byte. Assuming all data values");
					}
				}
				
				// Registration
				VeinTool veinTool = VeinTool.getByName(tool);
				if (isVeinable(veinTool, material, data)) {
					this.getVeinminableBlock(material).addMineableBy(veinTool);
				} else {
					this.registerVeinminableBlock(new VeinBlock(material, data), veinTool);
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
	 * Clear all localised data in the VeinMiner Manager
	 */
	public void clearLocalisedData() {
		this.disabledWorlds.clear();
		this.veinable.clear();
		
		for (VeinTool tool : VeinTool.values())
			tool.clearPlayerInformation();
	}
}