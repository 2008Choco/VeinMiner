package wtf.choco.veinminer;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.network.bukkit.BukkitProtocolConfiguration;
import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.anticheat.AntiCheatHookAAC;
import wtf.choco.veinminer.anticheat.AntiCheatHookAntiAura;
import wtf.choco.veinminer.anticheat.AntiCheatHookGrim;
import wtf.choco.veinminer.anticheat.AntiCheatHookLightAntiCheat;
import wtf.choco.veinminer.anticheat.AntiCheatHookMatrix;
import wtf.choco.veinminer.anticheat.AntiCheatHookNCP;
import wtf.choco.veinminer.anticheat.AntiCheatHookNegativity;
import wtf.choco.veinminer.anticheat.AntiCheatHookSpartan;
import wtf.choco.veinminer.anticheat.AntiCheatHookThemis;
import wtf.choco.veinminer.anticheat.AntiCheatHookVulcan;
import wtf.choco.veinminer.command.CommandBlocklist;
import wtf.choco.veinminer.command.CommandToollist;
import wtf.choco.veinminer.command.CommandVeinMiner;
import wtf.choco.veinminer.config.ConfigWrapper;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.config.impl.StandardVeinMinerConfiguration;
import wtf.choco.veinminer.config.migrator.ConfigMigrator;
import wtf.choco.veinminer.config.migrator.MigrationStep;
import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.data.PersistentStorageType;
import wtf.choco.veinminer.economy.EmptyEconomy;
import wtf.choco.veinminer.economy.SimpleEconomy;
import wtf.choco.veinminer.economy.SimpleVaultEconomy;
import wtf.choco.veinminer.integration.PlaceholderExpansionVeinMiner;
import wtf.choco.veinminer.integration.WorldGuardIntegration;
import wtf.choco.veinminer.language.JsonLanguageFile;
import wtf.choco.veinminer.language.LanguageFile;
import wtf.choco.veinminer.listener.BlockDropCollectionListener;
import wtf.choco.veinminer.listener.BreakBlockListener;
import wtf.choco.veinminer.listener.ItemDamageListener;
import wtf.choco.veinminer.listener.McMMOIntegrationListener;
import wtf.choco.veinminer.listener.PlayerDataListener;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.metrics.AntiCheat;
import wtf.choco.veinminer.metrics.StatTracker;
import wtf.choco.veinminer.network.VeinMinerBukkitChannelRegistrar;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPatternDefault;
import wtf.choco.veinminer.pattern.VeinMiningPatternStaircase;
import wtf.choco.veinminer.pattern.VeinMiningPatternStaircase.Direction;
import wtf.choco.veinminer.pattern.VeinMiningPatternTunnel;
import wtf.choco.veinminer.player.VeinMinerPlayerManager;
import wtf.choco.veinminer.tool.ToolCategoryRegistry;
import wtf.choco.veinminer.update.SpigotMCUpdateChecker;
import wtf.choco.veinminer.update.StandardVersionSchemes;
import wtf.choco.veinminer.update.UpdateChecker;

/**
 * The VeinMiner {@link JavaPlugin} class.
 */
public final class VeinMinerPlugin extends JavaPlugin {

    private static VeinMinerPlugin instance;

    private VeinMinerManager veinMinerManager = new VeinMinerManager(this);
    private VeinMinerPlayerManager playerManager = new VeinMinerPlayerManager();
    private ToolCategoryRegistry toolCategoryRegistry = new ToolCategoryRegistry(this);
    private PatternRegistry patternRegistry = new PatternRegistry();

    private SimpleEconomy economy = EmptyEconomy.INSTANCE;
    private PersistentDataStorage storage = PersistentStorageType.NONE.createStorage(this);

    private final UpdateChecker updateChecker = new SpigotMCUpdateChecker(this, 12038);
    private final List<AntiCheatHook> anticheatHooks = new ArrayList<>();

    private LanguageFile language;
    private ConfigWrapper categoriesConfig;
    private final VeinMinerConfiguration configuration = new StandardVeinMinerConfiguration(this);

    @Override
    public void onLoad() {
        instance = this;

        // Register all default patterns
        this.patternRegistry.register(VeinMiningPatternDefault.getInstance());
        this.patternRegistry.register(new VeinMiningPatternTunnel());
        this.patternRegistry.register(new VeinMiningPatternStaircase(Direction.UP));
        this.patternRegistry.register(new VeinMiningPatternStaircase(Direction.DOWN));

        // Setup the protocol
        VeinMiner.PROTOCOL.registerChannels(new VeinMinerBukkitChannelRegistrar(this));
        VeinMiner.PROTOCOL.configure(new BukkitProtocolConfiguration(this));

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            this.getLogger().info("Found WorldGuard. Registering custom region flag.");
            WorldGuardIntegration.init(this);
        }
    }

    @Override
    public void onEnable() {
        this.categoriesConfig = new ConfigWrapper(this, "categories.yml");

        if (!new File(getDataFolder(), "messages.json").exists()) {
            this.saveResource("messages.json", false);
        }
        this.language = new JsonLanguageFile(getDataFolder().toPath().resolve("messages.json"));
        this.language.reload(getLogger());

        this.saveDefaultConfig();

        ConfigMigrator configMigrator = new ConfigMigrator(this);
        configMigrator.addStep(MigrationStep.blockListsToCategoriesFile());
        int configMigrations = configMigrator.migrate();
        if (configMigrations > 0) {
            this.getLogger().info("Successfully ran " + configMigrations + " configuration migrations! Your configuration files are now up to date with the latest version of " + getName());
        }

        // Persistent storage
        this.setupPersistentStorage();

        // VeinMinerManager and ToolCategory loading into memory
        this.getLogger().info("Loading configuration options to local memory");
        this.veinMinerManager.reloadFromConfig();
        this.toolCategoryRegistry.reloadFromConfig();

        // Special case for server reloads
        this.storage.load(Bukkit.getOnlinePlayers().stream().map(playerManager::get).toList());

        // Update check
        if (getConfiguration().isPerformUpdateChecks()) {
            this.getLogger().info("Performing an update check!");

            this.updateChecker.checkForUpdates(StandardVersionSchemes.DECIMAL).thenAccept(result -> {
                result.getException().ifPresentOrElse(
                    e -> this.getLogger().info("Could not check for an update. Reason: ".formatted(e.getMessage())),
                    () -> {
                        if (result.isUpdateAvailable()) {
                            this.getLogger().info("Your version of VeinMiner is out of date! Version %s is available for download.".formatted(result.getNewestVersion()));
                        } else if (result.isUnreleased()) {
                            this.getLogger().info("You are running an unreleased version of VeinMiner! Proceed with caution!");
                        } else {
                            this.getLogger().info("You are on the latest version of VeinMiner!");
                        }
                    }
                );
            });
        }

        // Enable anti cheat hooks if required
        PluginManager manager = Bukkit.getPluginManager();
        this.registerAntiCheatHookIfEnabled(manager, "AAC5", AntiCheatHookAAC::new);
        this.registerAntiCheatHookIfEnabled(manager, "AntiAura", () -> new AntiCheatHookAntiAura(this));
        this.registerAntiCheatHookIfEnabled(manager, "GrimAC", () -> new AntiCheatHookGrim(this));
        this.registerAntiCheatHookIfEnabled(manager, "LightAntiCheat", AntiCheatHookLightAntiCheat::new);
        this.registerAntiCheatHookIfEnabled(manager, "Matrix", AntiCheatHookMatrix::new);
        this.registerAntiCheatHookIfEnabled(manager, "Negativity", AntiCheatHookNegativity::new);
        this.registerAntiCheatHookIfEnabled(manager, "NoCheatPlus", () -> new AntiCheatHookNCP(this));
        this.registerAntiCheatHookIfEnabled(manager, "Spartan", () -> new AntiCheatHookSpartan(this));
        this.registerAntiCheatHookIfEnabled(manager, "Themis", AntiCheatHookThemis::new);
        this.registerAntiCheatHookIfEnabled(manager, "Vulcan", AntiCheatHookVulcan::new);

        // Register commands
        this.getLogger().info("Registering commands");

        TabExecutor blocklistCommand = new CommandBlocklist(this), toollistCommand = new CommandToollist(this);
        this.registerCommand("blocklist", blocklistCommand);
        this.registerCommand("toollist", toollistCommand);
        this.registerCommand("veinminer", new CommandVeinMiner(this, blocklistCommand, toollistCommand));

        // Register events
        this.getLogger().info("Registering events");
        manager.registerEvents(new BlockDropCollectionListener(this), this);
        manager.registerEvents(new BreakBlockListener(this), this);
        manager.registerEvents(new ItemDamageListener(this), this);
        manager.registerEvents(new PlayerDataListener(this), this);

        Plugin mcMMOPlugin = manager.getPlugin("mcMMO");
        if (mcMMOPlugin != null && manager.isPluginEnabled("mcMMO")) {
            // Integrate with McMMO, but don't integrate with mcMMO-Classic, version 1.x
            if (!mcMMOPlugin.getDescription().getVersion().startsWith("1")) {
                manager.registerEvents(new McMMOIntegrationListener(this), this);
            } else if (getConfiguration().isNerfMcMMO()) {
                this.getLogger().warning("'NerfMcMMO' is enabled but McMMO-Classic is installed.");
                this.getLogger().warning("This version of McMMO is not supported and therefore this configuration option will not work!");
                this.getLogger().warning("Consider updating your version of McMMO.");
            }
        }

        Plugin placeholderAPIPlugin = manager.getPlugin("PlaceholderAPI");
        if (placeholderAPIPlugin != null && manager.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderExpansionVeinMiner(this).register();
        }

        // Vault integration
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.getLogger().info("Vault found. Attempting to enable economy support...");
            this.setEconomy(new SimpleVaultEconomy(this));
        } else {
            this.getLogger().info("Vault not found. Economy support suspended");
        }

        // Metrics
        if (getConfiguration().isMetricsEnabled()) {
            this.getLogger().info("Enabling Plugin Metrics");

            Metrics metrics = new Metrics(this, 1938); // https://bstats.org/what-is-my-plugin-id
            metrics.addCustomChart(new SingleLineChart("using_client_mod", playerManager::getPlayerCountUsingClientMod));
            StatTracker.setupMetrics(metrics);

            this.getLogger().info("Thanks for enabling Metrics! The anonymous stats are appreciated");
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Clearing localized data");

        this.getVeinMinerManager().clear();
        this.getPatternRegistry().unregisterAll();
        this.getToolCategoryRegistry().unregisterAll();

        this.storage.save(playerManager.getAll());

        this.anticheatHooks.clear();
    }

    /**
     * Get the {@link VeinMinerPlugin} instance.
     *
     * @return the vein miner instance
     */
    @NotNull
    public static VeinMinerPlugin getInstance() {
        return instance;
    }

    /**
     * Get VeinMiner's {@link VeinMinerManager} instance.
     *
     * @return the vein miner manager
     */
    @NotNull
    public VeinMinerManager getVeinMinerManager() {
        return veinMinerManager;
    }

    /**
     * Get VeinMiner's {@link ToolCategoryRegistry} instance.
     *
     * @return the tool category registry
     */
    @NotNull
    public ToolCategoryRegistry getToolCategoryRegistry() {
        return toolCategoryRegistry;
    }

    /**
     * Get VeinMiner's {@link PatternRegistry} instance.
     *
     * @return the pattern registry
     */
    @NotNull
    public PatternRegistry getPatternRegistry() {
        return patternRegistry;
    }

    /**
     * Get VeinMiner's {@link VeinMinerPlayerManager} instance.
     *
     * @return the player manager
     */
    @NotNull
    public VeinMinerPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Set the economy implementation used to handle economy transactions.
     *
     * @param economy the economy
     */
    public void setEconomy(@NotNull SimpleEconomy economy) {
        Preconditions.checkArgument(economy != null, "economy must not be null");
        this.economy = economy;
    }

    /**
     * Get the economy instance used to handle economy transactions.
     *
     * @return the economy
     */
    @NotNull
    public SimpleEconomy getEconomy() {
        return economy;
    }

    /**
     * Get VeinMiner's {@link PersistentDataStorage}.
     *
     * @return the persistent data storage
     */
    @NotNull
    public PersistentDataStorage getPersistentDataStorage() {
        return storage;
    }

    /**
     * Get VeinMiner's update checker.
     *
     * @return the update checker
     */
    @NotNull
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * Get VeinMiner's language file instance.
     *
     * @return the language
     */
    @NotNull
    public LanguageFile getLanguage() {
        return language;
    }

    /**
     * Get VeinMiner's categories.yml {@link ConfigWrapper} instance.
     *
     * @return the categories config wrapper
     */
    @NotNull
    public ConfigWrapper getCategoriesConfig() {
        return categoriesConfig;
    }

    /**
     * Get VeinMiner's {@link VeinMinerConfiguration} instance.
     *
     * @return the vein miner configuration
     */
    @NotNull
    public VeinMinerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Register an {@link AntiCheatHook} to VeinMiner. If an AntiCheatHook is registered, it is
     * expected to exempt players so as to not false-flag them with a fast-break violation while
     * they are vein mining.
     *
     * @param hook the hook to register
     *
     * @return true if successfully registered, false if the anti cheat is unsupported (according
     * to the hook itself, {@link AntiCheatHook#isSupported()})
     */
    public boolean registerAntiCheatHook(@NotNull AntiCheatHook hook) {
        Preconditions.checkNotNull(hook, "Cannot register a null anticheat hook implementation");

        if (!hook.isSupported()) {
            return false;
        }

        this.anticheatHooks.add(hook);
        return true;
    }

    /**
     * Get an immutable {@link List} of all {@link AntiCheatHook AntiCheatHooks}.
     *
     * @return all anti cheat hooks
     */
    @NotNull
    @UnmodifiableView
    public List<AntiCheatHook> getAnticheatHooks() {
        return Collections.unmodifiableList(anticheatHooks);
    }

    /**
     * Create a new {@link NamespacedKey} with VeinMiner's namespace.
     *
     * @param key the key
     *
     * @return a "veinminer" namespaced key
     */
    @NotNull
    public static NamespacedKey key(@NotNull String key) {
        return new NamespacedKey(instance, key);
    }

    private void registerCommand(@NotNull String commandName, @NotNull CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            return;
        }

        command.setExecutor(executor);
    }

    private void registerAntiCheatHookIfEnabled(@NotNull PluginManager manager, @NotNull String pluginName, @NotNull Supplier<@NotNull ? extends AntiCheatHook> hookSupplier) {
        if (!manager.isPluginEnabled(pluginName)) {
            return;
        }

        this.getLogger().info("Anti cheat detected. Enabling anti cheat support for \"" + pluginName + "\"");

        AntiCheatHook hook = hookSupplier.get();
        if (!registerAntiCheatHook(hook)) {
            this.getLogger().warning("The installed version of \"%s\" is incompatible with VeinMiner. Please contact the author of VeinMiner.".formatted(pluginName));
            return;
        }

        if (hook instanceof Listener hookListener) {
            manager.registerEvents(hookListener, this);
        }

        Plugin antiCheatPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (antiCheatPlugin != null) {
            StatTracker.addInstalledAntiCheat(new AntiCheat(antiCheatPlugin.getName(), antiCheatPlugin.getDescription().getVersion()));
        }
    }

    private void setupPersistentStorage() {
        try {
            this.storage = getConfiguration().getStorageType().createStorage(this);
            this.storage.init();

            if (storage.getType() != PersistentStorageType.NONE) {
                this.getLogger().info("Using \"" + storage.getType().getName() + "\" for persistent data storage.");
            } else {
                this.getLogger().warning("Not persistently storing player data. Is this intentional?");
            }
        } catch (Throwable e) {
            this.getLogger().severe("Could not setup persistent file storage. Player data cannot be saved nor loaded. Investigate IMMEDIATELY. Cause: " + e.getMessage());
        }
    }

}
