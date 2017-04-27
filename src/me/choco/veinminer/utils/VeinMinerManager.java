package me.choco.veinminer.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
	
	private Map<VeinTool, Set<VeinBlock>> veinable = new HashMap<>();
	private Map<VeinTool, Set<UUID>> veinminerOff = new HashMap<>();
	private Set<World> disabledWorlds = new HashSet<>();
	
	private VeinMiner plugin;
	
	public VeinMinerManager(VeinMiner plugin) {
		this.plugin = plugin;
		
		// Fill up HashMaps will raw values
		for (VeinTool tool : VeinTool.values()){
			this.veinable.put(tool, new HashSet<VeinBlock>());
			this.veinminerOff.put(tool, new HashSet<UUID>());
		}
	}
	
	/** 
	 * Register a VeinBlock to a specified VeinTool. The specified tool will be able 
	 * to break the registered block
	 * 
	 * @param tool - The parent tool to register the block to
	 * @param block - The block to register
	 */
	public void registerVeinminableBlock(VeinTool tool, VeinBlock block){
		this.veinable.get(tool).add(block);
	}
	
	/** 
	 * Unregister a specific material (with data). The specified tool will no longer 
	 * be able to break the unregistered block
	 * 
	 * @param tool - The parent tool to unregister the block from
	 * @param material - The material that should be unregistered
	 * @param data - The data that should be unregistered (-1 if none)
	 */
	public void unregisterVeinminableBlock(VeinTool tool, Material material, byte data){
		Iterator<VeinBlock> it = veinable.get(tool).iterator();
		while (it.hasNext()){
			VeinBlock block = it.next();
			if (block.getMaterial().equals(material) && (!block.hasSpecficData() || data == -1 || block.getData() == data)){
				it.remove();
				break;
			}
		}
	}
	
	/** 
	 * Unregister a specific material (with no specified data). The specified tool will
	 * no longer be able to break the unregistered block
	 * 
	 * @param tool - The parent tool to unregister the block from
	 * @param material - The material that should be unregistered
	 */
	public void unregisterVeinminableBlock(VeinTool tool, Material material){
		unregisterVeinminableBlock(tool, material, (byte) -1);
	}
	
	/** 
	 * Whether a material is able to be broken using VeinMiner with the specified tool
	 * 
	 * @param tool - The tool to check
	 * @param material - The material to check
	 * @return true if it is breakable with VeinMiner
	 */
	public boolean isVeinable(VeinTool tool, Material material){
		return isVeinable(tool, material, (byte) -1);
	}
	
	/** 
	 * Whether a material (with data) is able to be broken using VeinMiner with the specified tool
	 * 
	 * @param tool - The tool to check
	 * @param material - The material to check
	 * @param data - The data to check
	 * 
	 * @return true if it is breakable with VeinMiner
	 */
	public boolean isVeinable(VeinTool tool, Material material, byte data){
		if (!tool.equals(VeinTool.ALL) && isVeinable(VeinTool.ALL, material, data)) return true;
		
		for (VeinBlock block : veinable.get(tool))
			if (block.getMaterial().equals(material) && (!block.hasSpecficData() || data == -1 || block.getData() == data)) return true;
		return false;
	}
	
	/** 
	 * Get a list of all blocks able to be broken by VeinMiner from a specific tool
	 * 
	 * @param tool - The tool to check
	 * @return A set of all VeinMineable blocks from the tool
	 */
	public Set<VeinBlock> getVeinminableBlocks(VeinTool tool){
		return veinable.get(tool);
	}
	
	/** 
	 * Load all veinable blocks from the configuration file to memory without overriding current blocks
	 * 
	 * @see #loadVeinableBlocks(boolean)
	 */
	public void loadVeinableBlocks(){ 
		for (Set<VeinBlock> blocks : veinable.values()) 
			blocks.clear();
		
		for (String tool : plugin.getConfig().getConfigurationSection("BlockList").getKeys(false)){
			List<String> blocks = plugin.getConfig().getStringList("BlockList." + tool);
			
			for (String value : blocks){
				Material material = null;
				byte data = -1;
				
				String[] ids = value.split(";");
				
				//Material information
				material = Material.getMaterial(ids[0]);
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
				
				registerVeinminableBlock(VeinTool.getByName(tool), new VeinBlock(material, data));
			}
		}
	}
	
	/** 
	 * Get a set of all UUID's that currently have VeinMiner disabled
	 * 
	 * @param tool - The tool to get disabled players for
	 * @return a set of all disabled UUID's
	 */
	public Set<UUID> getPlayersWithVeinMinerDisabled(VeinTool tool){
		return veinminerOff.get(tool);
	}
	
	/** 
	 * Check whether a player has VeinMiner currently disabled or not
	 * 
	 * @param player - The player to check
	 * @param tool - The tool to check disabled players for
	 * 
	 * @return true if VeinMiner is disabled for the player
	 */
	public boolean hasVeinMinerDisabled(OfflinePlayer player, VeinTool tool){
		return hasVeinMinerDisabled(player.getUniqueId(), tool);
	}
	
	/** 
	 * Check whether a UUID has VeinMiner currently disabled or not
	 * 
	 * @param uuid - The UUID to check
	 * @param tool - The tool to check disabled UUID's for
	 * 
	 * @return true if VeinMiner is disabled for the UUID
	 */
	public boolean hasVeinMinerDisabled(UUID uuid, VeinTool tool){
		return veinminerOff.get(tool).contains(uuid);
	}
	
	/** 
	 * Check whether a player has VeinMiner currently enabled or not
	 * 
	 * @param player - The player to check
	 * @param tool - The tool to check enabled players for
	 * 
	 * @return true if VeinMiner is enabled for the player
	 */
	public boolean hasVeinMinerEnabled(OfflinePlayer player, VeinTool tool){
		return hasVeinMinerEnabled(player.getUniqueId(), tool);
	}
	
	/** 
	 * Check whether a UUID has VeinMiner currently enabled or not
	 * 
	 * @param uuid - The UUID to check
	 * @param tool - The tool to check enabled UUID's for
	 * 
	 * @return true if VeinMiner is enabled for the UUID
	 */
	public boolean hasVeinMinerEnabled(UUID uuid, VeinTool tool){
		return !hasVeinMinerDisabled(uuid, tool);
	}
	
	/** 
	 * Toggle whether VeinMiner is enabled or disabled for a specific player
	 * 
	 * @param player - The player to toggle
	 * @param tool - The tool in which should be toggled
	 */
	public void toggleVeinMiner(OfflinePlayer player, VeinTool tool){
		toggleVeinMiner(player.getUniqueId(), tool);
	}
	
	/** 
	 * Toggle whether VeinMiner is enabled or disabled for a specific UUID
	 * 
	 * @param uuid - The UUID to toggle
	 * @param tool - The tool in which should be toggled
	 */
	public void toggleVeinMiner(UUID uuid, VeinTool tool){
		if (hasVeinMinerDisabled(uuid, tool)) veinminerOff.get(tool).remove(uuid);
		else veinminerOff.get(tool).add(uuid);
	}
	
	/** 
	 * Set whether a players VeinMiner should be enabled or not
	 * 
	 * @param player - The player to toggle
	 * @param tool - The tool to affect
	 * @param toggle - Whether it should be enabled (true) or disabled (false)
	 */
	public void toggleVeinMiner(OfflinePlayer player, VeinTool tool, boolean toggle){
		toggleVeinMiner(player.getUniqueId(), tool, toggle);
	}
	
	/** 
	 * Set whether a UUID's VeinMiner should be enabled or not
	 * 
	 * @param uuid - The UUID to toggle
	 * @param tool - The tool to affect
	 * @param toggle - Whether it should be enabled (true) or disabled (false)
	 */
	public void toggleVeinMiner(UUID uuid, VeinTool tool, boolean toggle){
		if (toggle && hasVeinMinerDisabled(uuid, tool)) veinminerOff.get(tool).remove(uuid);
		else if (!toggle && hasVeinMinerEnabled(uuid, tool)) veinminerOff.get(tool).add(uuid);
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
			disabledWorlds.add(world);
		}
	}
	
	/** 
	 * Check whether a world has VeinMiner disabled or not
	 * 
	 * @param world - The world to check
	 * @return true if the world has VeinMiner disabled
	 */
	public boolean isDisabledInWorld(World world){
		return disabledWorlds.contains(world);
	}
	
	/** 
	 * Get a list of all worlds in which VeinMiner is disabled
	 * 
	 * @return a list of all disabled worlds
	 */
	public Set<World> getDisabledWorlds(){
		return disabledWorlds;
	}
	
	/** 
	 * Disable veinminer in a specific world
	 * 
	 * @param world - The world to disable
	 */
	public void setDisabledInWorld(World world){
		if (!isDisabledInWorld(world)) disabledWorlds.add(world);
	}
	
	/** 
	 * Enable VeinMiner in a specific world
	 * 
	 * @param world - The world to disable
	 */
	public void setEnabledInWorld(World world){
		if (isDisabledInWorld(world)) disabledWorlds.remove(world);
	}
}