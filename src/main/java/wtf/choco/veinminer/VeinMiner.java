package wtf.choco.veinminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.anticheat.AntiCheatHookAAC;
import wtf.choco.veinminer.anticheat.AntiCheatHookAntiAura;
import wtf.choco.veinminer.anticheat.AntiCheatHookNCP;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.commands.VeinMinerCmd;
import wtf.choco.veinminer.commands.VeinMinerCmdTabCompleter;
import wtf.choco.veinminer.events.BreakBlockListener;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.utils.ReflectionUtil;
import wtf.choco.veinminer.utils.UpdateChecker;
import wtf.choco.veinminer.utils.UpdateChecker.UpdateReason;
import wtf.choco.veinminer.utils.metrics.StatTracker;

@Permission(name = "veinminer.veinmine.*", desc = "Allow the use of VeinMiner for all tools", defaultValue = PermissionDefault.TRUE, children = {
	@ChildPermission(name = "veinminer.veinmine.pickaxe"),
	@ChildPermission(name = "veinminer.veinmine.axe"),
	@ChildPermission(name = "veinminer.veinmine.shovel"),
	@ChildPermission(name = "veinminer.veinmine.hoe"),
	@ChildPermission(name = "veinminer.veinmine.shears"),
	@ChildPermission(name = "veinminer.veinmine.hand")
})
@Author("2008Choco")
@Description("Aims to recreate the Forge mod, VeinMiner, in an efficient, flexible and feature-filled way")
@SoftDependency("NoCheatPlus") @SoftDependency("AAC") @SoftDependency("AntiAura")
@ApiVersion(Target.v1_13)
@Plugin(name = "VeinMiner", version = "1.12.3")
public class VeinMiner extends JavaPlugin {
	
	public static final String CHAT_PREFIX = ChatColor.BLUE.toString() + ChatColor.BOLD + "VeinMiner | " + ChatColor.GRAY;
	
	private static VeinMiner instance;
	
	private final List<AntiCheatHook> anticheatHooks = new ArrayList<>();
	
	private VeinMinerManager manager;
	private PatternRegistry patternRegistry;
	
	@Override
	public void onEnable() {
		instance = this;
		this.manager = new VeinMinerManager(this);
		this.patternRegistry = new PatternRegistry();
		this.saveDefaultConfig();
		
		ReflectionUtil.loadNMSClasses(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
		
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
		
		// Update check (https://www.spigotmc.org/resources/veinminer.12038/)
		this.getLogger().info("Performing an update check!");
		UpdateChecker.init(this, 12038).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				this.getLogger().info(String.format("An update is available! VeinMiner %s may be downloaded on SpigotMC", result.getNewestVersion()));
				return;
			}
			
			UpdateReason reason = result.getReason();
			if (reason == UpdateReason.UP_TO_DATE) {
				this.getLogger().info(String.format("Your version of VeinMiner (%s) is up to date!", result.getNewestVersion()));
			} else if (reason == UpdateReason.UNRELEASED_VERSION) {
				this.getLogger().info(String.format("Your version of VeinMiner (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
			} else {
				this.getLogger().warning("Could not check for a new version of VeinMiner. Reason: " + reason);
			}
		});
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