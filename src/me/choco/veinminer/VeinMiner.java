package me.choco.veinminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

import me.choco.veinminer.anticheat.AntiCheatHook;
import me.choco.veinminer.anticheat.AntiCheatHookAAC;
import me.choco.veinminer.anticheat.AntiCheatHookAntiAura;
import me.choco.veinminer.anticheat.AntiCheatHookNCP;
import me.choco.veinminer.commands.VeinMinerCmd;
import me.choco.veinminer.commands.VeinMinerCmdTabCompleter;
import me.choco.veinminer.events.BreakBlockListener;
import me.choco.veinminer.pattern.PatternRegistry;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.metrics.Metrics;
import me.choco.veinminer.utils.metrics.StatTracker;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class VeinMiner extends JavaPlugin {
	
	private static VeinMiner instance;
	
	private final List<AntiCheatHook> anticheatHooks = new ArrayList<>();
	private final double antiAuraVersion = -1;
	
	private VeinMinerManager manager;
	private PatternRegistry patternRegistry;
	
	@Override
	public void onEnable() {
		instance = this;
		this.manager = new VeinMinerManager(this);
		this.patternRegistry = new PatternRegistry();
		this.saveDefaultConfig();
		
		// Enable anticheat hooks if required
		PluginManager manager = Bukkit.getPluginManager();
		if (manager.isPluginEnabled("NoCheatPlus")) {
			this.anticheatHooks.add(new AntiCheatHookNCP());
		}
		if (manager.isPluginEnabled("AntiAura")) {
			this.anticheatHooks.add(new AntiCheatHookAntiAura());
		}
		if (manager.isPluginEnabled("AAC")) {
			AntiCheatHookAAC aacHook = new AntiCheatHookAAC();
			
			manager.registerEvents(aacHook, this);
			this.anticheatHooks.add(aacHook);
		}
		
		// Register events
		this.getLogger().info("Registering events");
		manager.registerEvents(new BreakBlockListener(this), this);
		
		// Register commands
		this.getLogger().info("Registering commands");
		PluginCommand veinminerCmd = getCommand("veinminer");
		veinminerCmd.setExecutor(new VeinMinerCmd(this));
		veinminerCmd.setTabCompleter(new VeinMinerCmdTabCompleter(this));
		
		// Metrics
		if (getConfig().getBoolean("MetricsEnabled", true)) {
			this.getLogger().info("Enabling Plugin Metrics");
			
			Metrics metrics = new Metrics(this);
			metrics.addCustomChart(new Metrics.AdvancedPie("blocks_veinmined", StatTracker.get()::getVeinMinedCountAsData));
			
			this.getLogger().info("Thank you for enabling Metrics! I greatly appreciate the use of plugin statistics");
		}
		
		// Load blocks to the veinable list
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
		this.anticheatHooks.clear();
	}
	
	/**
	 * Get an instance of the main VeinMiner class (for VeinMiner API usages).
	 * 
	 * @return an instance of the VeinMiner class
	 */
	public static VeinMiner getPlugin() {
		return instance;
	}
	
	/**
	 * Get the VeinMiner Manager used to keep track of Veinminable blocks, and other utilities.
	 * 
	 * @return an instance of the VeinMiner manager
	 */
	public VeinMinerManager getVeinMinerManager() {
		return manager;
	}
	
	/**
	 * Get the pattern registry used to register custom vein mining patterns.
	 * 
	 * @return an instance of the pattern registry
	 */
	public PatternRegistry getPatternRegistry() {
		return patternRegistry;
	}
	
	/**
	 * Get the active version of AntiAura. If the plugin is not enabled, 0.0 will be returned.
	 * 
	 * @return the AntiAura version. 0.0 if not enabled
	 */
	public double getAntiAuraVersion() {
		return antiAuraVersion;
	}
	
	/**
	 * Register an anticheat hook to VeinMiner. Hooks should be registered for all anticheat plugins
	 * as to support VeinMining and not false-flag players with fast-break.
	 * 
	 * @param hook the hook to register
	 */
	public void registerAntiCheatHook(AntiCheatHook hook) {
		Preconditions.checkNotNull(hook, "Cannot register a null anticheat hook implementation");
		
		for (AntiCheatHook anticheatHook : anticheatHooks) {
			if (anticheatHook.getPluginName().equals(hook.getPluginName())) {
				throw new IllegalArgumentException("Anticheat Hook for plugin " + anticheatHook.getPluginName() + " already registered");
			}
		}
		
		this.anticheatHooks.add(hook);
	}
	
	/**
	 * Get an immutable list of all anti cheat hooks.
	 * 
	 * @return all anticheat hooks
	 */
	public List<AntiCheatHook> getAnticheatHooks() {
		return Collections.unmodifiableList(anticheatHooks);
	}
	
}