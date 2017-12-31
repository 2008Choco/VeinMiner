package me.choco.veinminer;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;

import me.choco.veinminer.commands.VeinMinerCmd;
import me.choco.veinminer.commands.VeinMinerCmdTabCompleter;
import me.choco.veinminer.events.AntiCheatSupport;
import me.choco.veinminer.events.BreakBlockListener;
import me.choco.veinminer.pattern.PatternRegistry;
import me.choco.veinminer.utils.Metrics;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.versions.NMSAbstract;
import me.choco.veinminer.utils.versions.NMSAbstractDefault;
import me.choco.veinminer.utils.versions.v1_10.NMSAbstract1_10_R1;
import me.choco.veinminer.utils.versions.v1_11.NMSAbstract1_11_R1;
import me.choco.veinminer.utils.versions.v1_12.NMSAbstract1_12_R1;
import me.choco.veinminer.utils.versions.v1_8.NMSAbstract1_8_R1;
import me.choco.veinminer.utils.versions.v1_8.NMSAbstract1_8_R2;
import me.choco.veinminer.utils.versions.v1_8.NMSAbstract1_8_R3;
import me.choco.veinminer.utils.versions.v1_9.NMSAbstract1_9_R1;
import me.choco.veinminer.utils.versions.v1_9.NMSAbstract1_9_R2;

public class VeinMiner extends JavaPlugin {
	
	private static VeinMiner instance;
	private AntiCheatSupport antiCheatSupport;
	
	private boolean ncpEnabled, aacEnabled, antiAuraEnabled;
	private double antiAuraVersion = -1;
	
	private VeinMinerManager manager;
	private PatternRegistry patternRegistry;
	private NMSAbstract nmsAbstract;
	
	@Override
	public void onEnable() {
		// Attempt to set up the version independence manager
		if (!setupNMSAbstract()) {
			this.getLogger().warning("VeinMiner is not officially supported on this version of Minecraft");
			this.getLogger().warning("Some features may not work properly");
		}

		instance = this;
		this.manager = new VeinMinerManager(this);
		this.patternRegistry = new PatternRegistry();
		
		// Check for soft-dependencies
		PluginManager manager = Bukkit.getPluginManager();
		this.ncpEnabled = manager.isPluginEnabled("NoCheatPlus");
		this.aacEnabled = manager.isPluginEnabled("AAC");
		this.antiAuraEnabled = manager.isPluginEnabled("AntiAura");
		
		this.saveDefaultConfig();
		
		//Register events
		this.getLogger().info("Registering events");
		manager.registerEvents(new BreakBlockListener(this), this);
		if (aacEnabled) manager.registerEvents((antiCheatSupport = new AntiCheatSupport()), this);
		
		//Register commands
		this.getLogger().info("Registering commands");
		PluginCommand veinminerCmd = getCommand("veinminer");
		veinminerCmd.setExecutor(new VeinMinerCmd(this));
		veinminerCmd.setTabCompleter(new VeinMinerCmdTabCompleter(this));
		
		//Metrics
		if (getConfig().getBoolean("MetricsEnabled", true)) {
			this.getLogger().info("Enabling Plugin Metrics");
			new Metrics(this);
			this.getLogger().info("Thank you for enabling Metrics! I greatly appreciate the use of plugin statistics");
		}

		//Load blocks to the veinable list
		this.getLogger().info("Loading configuration options to local memory");
		this.manager.loadVeinableBlocks();
		this.manager.loadDisabledWorlds();
		this.manager.loadMaterialAliases();
	}
	
	@Override
	public void onDisable() {
		this.getLogger().info("Clearing localized data");
		this.manager.clearLocalisedData();
		this.patternRegistry.clearPatterns();
		
		if (antiCheatSupport != null) antiCheatSupport.clearExemptedUsers();
	}
	
	/** 
	 * Get an instance of the main VeinMiner class (for VeinMiner API usages)
	 * 
	 * @return an instance of the VeinMiner class
	 */
	public static VeinMiner getPlugin() {
		return instance;
	}
	
	/** 
	 * Get the VeinMiner Manager used to keep track of veinminable blocks, and other utilities
	 * 
	 * @return an instance of the VeinMiner manager
	 */
	public VeinMinerManager getVeinMinerManager() {
		return manager;
	}
	
	/**
	 * Get the pattern registry used to register custom vein mining patterns
	 * 
	 * @return an instance of the pattern registry
	 */
	public PatternRegistry getPatternRegistry() {
		return patternRegistry;
	}
	
	/** 
	 * Get the interface that manages all things related to version independence
	 * 
	 * @return the current NMSAbstract implementation
	 */
	public NMSAbstract getNMSAbstract() {
		return nmsAbstract;
	}
	
	/** 
	 * Get an instance of the listener used to prevent false-positives on anti-cheat plugins 
	 * (Only Konsolas' Advanced Anti-Cheat is supported in this class as of now)
	 * 
	 * @return an instance of the anti cheat listener
	 */
	public AntiCheatSupport getAntiCheatSupport() {
		return antiCheatSupport;
	}
	
	/** 
	 * Check whether NoCheatPlus is currently enabled on the server or not
	 * 
	 * @return true if NCP is enabled
	 */
	public boolean isNCPEnabled() {
		return ncpEnabled;
	}
	
	/** 
	 * Check whether Advanced Anti-Cheat is currently enabled on the server or not
	 * 
	 * @return true if AAC is enabled
	 */
	public boolean isAACEnabled() {
		return aacEnabled;
	}
	
	/** 
	 * Check whether Anti Aura is currently enabled on the server or not
	 * 
	 * @return true if Anti Aura is enabled
	 */
	public boolean isAntiAuraEnabled() {
		if (antiAuraEnabled && antiAuraVersion == -1)
			this.antiAuraVersion = NumberUtils.toDouble(Bukkit.getPluginManager().getPlugin("AntiAura").getDescription().getVersion(), Double.NaN);
		
		return antiAuraEnabled && this.antiAuraVersion >= 10.83; // API implemented in 10.83
	}
	
	private final boolean setupNMSAbstract() {
		Preconditions.checkArgument(nmsAbstract == null, "Cannot setup NMSAbstract implementation more than once");
		
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		switch (version) {
			case "v1_8_R1": nmsAbstract = new NMSAbstract1_8_R1(); break; // 1.8.0 - 1.8.2
			case "v1_8_R2": nmsAbstract = new NMSAbstract1_8_R2(); break; // 1.8.3
			case "v1_8_R3": nmsAbstract = new NMSAbstract1_8_R3(); break; // 1.8.4 - 1.8.8
			case "v1_9_R1": nmsAbstract = new NMSAbstract1_9_R1(); break; // 1.9.0 - 1.9.3
			case "v1_9_R2": nmsAbstract = new NMSAbstract1_9_R2(); break; // 1.9.4
			case "v1_10_R1": nmsAbstract = new NMSAbstract1_10_R1(); break; // 1.10.0 - 1.10.2
			case "v1_11_R1": nmsAbstract = new NMSAbstract1_11_R1(); break; // 1.11.0 - 1.11.2
			case "v1_12_R1": nmsAbstract = new NMSAbstract1_12_R1(); break; // 1.12.0-Pre2+
			default: nmsAbstract = new NMSAbstractDefault(version);
		}
		
		return !(nmsAbstract instanceof NMSAbstractDefault);
	}
	
}