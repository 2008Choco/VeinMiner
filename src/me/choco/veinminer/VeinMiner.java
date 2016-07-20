package me.choco.veinminer;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.events.BreakBlockListener;
import me.choco.veinminer.utils.Metrics;
import me.choco.veinminer.utils.Metrics.Graph;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.VeinsBrokenPlotter;
import me.choco.veinminer.utils.commands.VeinMinerCmd;
import me.choco.veinminer.utils.commands.VeinMinerCmdTabCompleter;
import me.choco.veinminer.utils.versions.VersionBreaker;
/* ----------------
 * Version breakers
 */
import me.choco.veinminer.utils.versions.v1_10.VersionBreaker1_10_R1;
import me.choco.veinminer.utils.versions.v1_8.VersionBreaker1_8_R2;
import me.choco.veinminer.utils.versions.v1_8.VersionBreaker1_8_R3;
import me.choco.veinminer.utils.versions.v1_9.VersionBreaker1_9_R1;
import me.choco.veinminer.utils.versions.v1_9.VersionBreaker1_9_R2;

public class VeinMiner extends JavaPlugin{
	
	private static VeinMiner instance;
	
	private VeinMinerManager manager;
	private VersionBreaker versionBreaker;
	
	@Override
	public void onEnable(){
		// Attempt to set up the version independence manager
		if (!setupVersionBreaker()){
			this.getLogger().severe("VEINMINER WILL NOT WORK ON YOUR SERVER VERSION");
			this.getLogger().severe("PLEASE UPDATE YOUR SERVER AS SOON AS POSSIBLE");
		}
		
		instance = this;
		saveDefaultConfig();
		this.manager = new VeinMinerManager(this);
		
		//Register events
		this.getLogger().info("Registering events");
		Bukkit.getServer().getPluginManager().registerEvents(new BreakBlockListener(this), this);
		
		//Register commands
		this.getLogger().info("Registering commands");
		Bukkit.getPluginCommand("veinminer").setExecutor(new VeinMinerCmd(this));
		Bukkit.getPluginCommand("veinminer").setTabCompleter(new VeinMinerCmdTabCompleter());
		
		//Metrics
		if (getConfig().getBoolean("MetricsEnabled")){
			this.getLogger().info("Enabling Plugin Metrics");
		    try{
		        Metrics metrics = new Metrics(this);
		        
		        // Custom graph registration
		        Graph graph = metrics.createGraph("Veins Broken");
		        graph.addPlotter(new VeinsBrokenPlotter());
		        
		        if (metrics.start())
		        	this.getLogger().info("Thank you for enabling Metrics! I greatly appreciate the use of plugin statistics");
		    }
		    catch (IOException e){
		    	e.printStackTrace();
		        getLogger().warning("Could not enable Plugin Metrics. If issues continue, please put in a ticket on the "
		        	+ "VeinMiner development page");
		    }
		}
		
		//Block list modifications
		this.getLogger().info("Running last minute blocklist modifications");
		for (String tool : getConfig().getConfigurationSection("BlockList").getKeys(false)){
			List<String> list = getConfig().getStringList("BlockList." + tool);
			if (list.contains("REDSTONE_ORE") && !(list.contains("GLOWING_REDSTONE_ORE"))){
				list.add("GLOWING_REDSTONE_ORE");
				this.getLogger().info("Adding \"GLOWING_REDSTONE_ORE\" to the list of breakable blocks");
			}
			else if (list.contains("GLOWING_REDSTONE_ORE") && !(list.contains("REDSTONE_ORE"))){
				list.add("REDSTONE_ORE");
				this.getLogger().info("Adding \"REDSTONE_ORE\" to the list of breakable blocks");
			}
			getConfig().set("BlockList." + tool, list);
		}
		saveConfig(); reloadConfig();
		
		//Load blocks to the veinable list
		this.getLogger().info("Loading configuration options to local memory");
		manager.loadVeinableBlocks();
		manager.loadDisabledWorlds();
	}
	
	@Override
	public void onDisable() {
		for (VeinTool tool : VeinTool.values()){
			manager.getVeinminableBlocks(tool).clear();
			manager.getPlayersWithVeinMinerDisabled(tool).clear();
		}
		manager.getDisabledWorlds().clear();
	}
	
	/** Get an instance of the main VeinMiner class (for VeinMiner API usages)
	 * @return an instance of the VeinMiner class
	 */
	public static VeinMiner getPlugin(){
		return instance;
	}
	
	/** Get the VeinMiner Manager used to keep track of veinminable blocks, and other utilities
	 * @return an instance of the VeinMiner manager
	 */
	public VeinMinerManager getVeinMinerManager() {
		return manager;
	}
	
	/** Get the interface that manages all things related to version independence
	 * <li> Breaking blocks
	 * <li> Getting item in hand
	 * @return the VersionBreaker interface
	 */
	public VersionBreaker getVersionBreaker() {
		return versionBreaker;
	}
	
	private final boolean setupVersionBreaker(){
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        if (version.equals("v1_8_R2")){ // 1.8.2 - 1.8.6
        	this.versionBreaker = new VersionBreaker1_8_R2();
        	return true;
        }else if (version.equals("v1_8_R3")){ // 1.8.7 - 1.8.9
        	this.versionBreaker = new VersionBreaker1_8_R3();
        	return true;
        }else if (version.equalsIgnoreCase("v1_9_R1")){ // 1.9.0 - 1.9.3
        	this.versionBreaker = new VersionBreaker1_9_R1();
        	return true;
        }else if (version.equalsIgnoreCase("v1_9_R2")){ // 1.9.4
        	this.versionBreaker = new VersionBreaker1_9_R2();
        	return true;
        }else if (version.equalsIgnoreCase("v1_10_R1")){ // 1.10.0 - 1.10.2
        	this.versionBreaker = new VersionBreaker1_10_R1();
        	return true;
        }
        return false;
	}
}

/* CHANGELOG 1.10.2:
 * Added Javadoc documentation to the VersionBreaker class (Should only be used for VeinMiner purposes, but it's available for public API)
 * Added a bit more Javadoc documentation to the VeinBlock and VeinTool classes. Just in case
 * Added a new custom "Veins Broken" graph to the Metrics website to display how many veins have been broken using VeinMiner. 
 *     - This is obviously a test, but it might be interesting statistics :) I have never made a custom graph before
 * Added a little message for those who enable Metrics in the console on startup (<3 Thanks to you owners that do have it enabled :D)
 */