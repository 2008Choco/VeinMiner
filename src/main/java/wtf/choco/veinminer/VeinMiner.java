package wtf.choco.veinminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.anticheat.AntiCheatHookAAC;
import wtf.choco.veinminer.anticheat.AntiCheatHookAntiAura;
import wtf.choco.veinminer.anticheat.AntiCheatHookNCP;
import wtf.choco.veinminer.anticheat.AntiCheatHookSpartan;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.commands.VeinMinerCmd;
import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.VMPlayerData;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.listener.BreakBlockListener;
import wtf.choco.veinminer.pattern.PatternExpansive;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.PatternThorough;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.utils.Chat;
import wtf.choco.veinminer.utils.ConfigWrapper;
import wtf.choco.veinminer.utils.ReflectionUtil;
import wtf.choco.veinminer.utils.UpdateChecker;
import wtf.choco.veinminer.utils.UpdateChecker.UpdateReason;
import wtf.choco.veinminer.utils.metrics.StatTracker;

public class VeinMiner extends JavaPlugin {

    public static final String CHAT_PREFIX = ChatColor.BLUE.toString() + ChatColor.BOLD + "VeinMiner | " + ChatColor.GRAY;
    public static final Pattern BLOCK_DATA_PATTERN = Pattern.compile("(?:[\\w:]+)(?:\\[(.+=.+)+\\])*");

    private static VeinMiner instance;

    private final List<AntiCheatHook> anticheatHooks = new ArrayList<>();

    private VeinMinerManager manager;
    private PatternRegistry patternRegistry;

    private ConfigWrapper categoriesConfig;

    @Override
    public void onEnable() {
        instance = this;
        this.manager = new VeinMinerManager(this);
        Chat.PREFIXED.setPrefix(CHAT_PREFIX);

        // Configuration handling
        this.saveDefaultConfig();
        this.categoriesConfig = new ConfigWrapper(this, getDataFolder(), "categories.yml");

        // Pattern registration
        this.patternRegistry = new PatternRegistry();
        this.patternRegistry.registerPattern(PatternThorough.get());
        this.patternRegistry.registerPattern(PatternExpansive.get());

        ReflectionUtil.init(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);

        // Enable anticheat hooks if required
        PluginManager manager = Bukkit.getPluginManager();
        this.registerAntiCheatHookIfEnabled(manager, "NoCheatPlus", AntiCheatHookNCP::new);
        this.registerAntiCheatHookIfEnabled(manager, "AntiAura", AntiCheatHookAntiAura::new);
        this.registerAntiCheatHookIfEnabled(manager, "AAC", AntiCheatHookAAC::new);
        this.registerAntiCheatHookIfEnabled(manager, "Spartan", AntiCheatHookSpartan::new);

        // Register events
        this.getLogger().info("Registering events");
        manager.registerEvents(new BreakBlockListener(this), this);

        // Register commands
        this.getLogger().info("Registering commands");
        new VeinMinerCmd(this).assignTo(getCommand("veinminer"));

        // Metrics
        if (getConfig().getBoolean("MetricsEnabled", true)) {
            this.getLogger().info("Enabling Plugin Metrics");

            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.AdvancedPie("blocks_veinmined", StatTracker.get()::getVeinMinedCountAsData));

            this.getLogger().info("Thanks for enabling Metrics! The anonymous stats are appreciated");
        }

        // Load blocks to the veinable list
        this.getLogger().info("Loading configuration options to local memory");
        this.manager.loadToolCategories();
        this.manager.loadVeinableBlocks();
        this.manager.loadMaterialAliases();

        // Update check (https://www.spigotmc.org/resources/veinminer.12038/)
        UpdateChecker updateChecker = UpdateChecker.init(this, 12038);
        if (getConfig().getBoolean("PerformUpdateChecks")) {
            this.getLogger().info("Performing an update check!");
            updateChecker.requestUpdateCheck().whenComplete((result, exception) -> {
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
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Clearing localized data");
        this.manager.clearLocalisedData();
        this.patternRegistry.clearPatterns();
        this.anticheatHooks.clear();
        VMPlayerData.clearCache();
        VeinBlock.clearCache();
        ToolCategory.clearCategories();
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
     * Get an instance of the categories configuration file.
     *
     * @return the categories config
     */
    @NotNull
    public ConfigWrapper getCategoriesConfig() {
        return categoriesConfig;
    }

    /**
     * Register an anticheat hook to VeinMiner. Hooks should be registered for all anticheat plugins
     * as to support VeinMining and not false-flag players with fast-break.
     *
     * @param hook the hook to register
     *
     * @return true if successful, false if a hook for a plugin with a similar name is already registered
     * or the hook is unsupported according to {@link AntiCheatHook#isSupported()}.
     */
    public boolean registerAntiCheatHook(@NotNull AntiCheatHook hook) {
        Preconditions.checkNotNull(hook, "Cannot register a null anticheat hook implementation");

        if (!hook.isSupported()) {
            return false;
        }

        for (AntiCheatHook anticheatHook : anticheatHooks) {
            if (anticheatHook.getPluginName().equals(hook.getPluginName())) {
                return false;
            }
        }

        return anticheatHooks.add(hook);
    }

    /**
     * Get an immutable list of all anti cheat hooks.
     *
     * @return all anticheat hooks
     */
    @NotNull
    public List<AntiCheatHook> getAnticheatHooks() {
        return Collections.unmodifiableList(anticheatHooks);
    }

    /**
     * Create an {@link AlgorithmConfig} based on the current root settings defined in the
     * config.yml. These settings are considered "default".
     *
     * @return the default algorithm config
     */
    @NotNull
    public AlgorithmConfig createDefaultAlgorithmConfig() {
        FileConfiguration config = getConfig();

        AlgorithmConfig algorithmConfig = new AlgorithmConfig().defaultValues();
        if (config.contains("RepairFriendlyVeinMiner")) {
            algorithmConfig.repairFriendly(config.getBoolean("RepairFriendlyVeinMiner"));
        }
        if (config.contains("IncludeEdges")) {
            algorithmConfig.includeEdges(config.getBoolean("IncludeEdges"));
        }
        if (config.contains("MaxVeinSize")) {
            algorithmConfig.maxVeinSize(Math.max(config.getInt("MaxVeinSize"), 1));
        }
        if (config.contains("DisabledWorlds")) {
            for (String worldName : config.getStringList("DisabledWorlds")) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                algorithmConfig.disabledWorld(world);
            }
        }

        return algorithmConfig;
    }

    private void registerAntiCheatHookIfEnabled(@NotNull PluginManager manager, @NotNull String pluginName, @NotNull Supplier<@NotNull ? extends AntiCheatHook> hookSupplier) {
        if (!manager.isPluginEnabled(pluginName)) {
            return;
        }

        AntiCheatHook hook = hookSupplier.get();
        if (!registerAntiCheatHook(hook)) {
            this.getLogger().info("Tried to register hook for plugin " + pluginName + " but one was already registered. Not overriding...");
            return;
        }

        if (hook instanceof Listener) {
            manager.registerEvents((Listener) hook, this);
        }

        this.getLogger().info("Anti cheat detected. Enabling anti cheat support for \"" + hook.getPluginName() + "\"");
    }

}
