package wtf.choco.veinminer;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.anticheat.AntiCheatHookAAC;
import wtf.choco.veinminer.anticheat.AntiCheatHookAntiAura;
import wtf.choco.veinminer.anticheat.AntiCheatHookLightAntiCheat;
import wtf.choco.veinminer.anticheat.AntiCheatHookMatrix;
import wtf.choco.veinminer.anticheat.AntiCheatHookNCP;
import wtf.choco.veinminer.anticheat.AntiCheatHookSpartan;
import wtf.choco.veinminer.command.CommandBlocklist;
import wtf.choco.veinminer.command.CommandToollist;
import wtf.choco.veinminer.command.CommandVeinMiner;
import wtf.choco.veinminer.economy.SimpleVaultEconomy;
import wtf.choco.veinminer.integration.PlaceholderExpansionVeinMiner;
import wtf.choco.veinminer.integration.WorldGuardIntegration;
import wtf.choco.veinminer.listener.BreakBlockListener;
import wtf.choco.veinminer.listener.ItemCollectionListener;
import wtf.choco.veinminer.listener.McMMOIntegrationListener;
import wtf.choco.veinminer.listener.PlayerDataListener;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.manager.VeinMinerPlayerManager;
import wtf.choco.veinminer.metrics.AntiCheat;
import wtf.choco.veinminer.metrics.StatTracker;
import wtf.choco.veinminer.network.BukkitChannelHandler;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.platform.BukkitServerPlatform;
import wtf.choco.veinminer.tool.ToolCategoryRegistry;
import wtf.choco.veinminer.util.ConfigWrapper;
import wtf.choco.veinminer.util.UpdateChecker;
import wtf.choco.veinminer.util.UpdateChecker.UpdateReason;
import wtf.choco.veinminer.util.VMConstants;

/**
 * The VeinMiner {@link JavaPlugin} class.
 */
public final class VeinMinerPlugin extends JavaPlugin {

    private static VeinMinerPlugin instance;

    private final List<AntiCheatHook> anticheatHooks = new ArrayList<>();

    private ConfigWrapper categoriesConfig;

    @Override
    public void onLoad() {
        instance = this;

        VeinMinerServer.getInstance().onLoad(BukkitServerPlatform.getInstance());
        VeinMiner.PROTOCOL.registerChannels(new BukkitChannelHandler(this));

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            this.getLogger().info("Found WorldGuard. Registering custom region flag.");
            WorldGuardIntegration.init(this);
        }
    }

    @Override
    public void onEnable() {
        this.categoriesConfig = new ConfigWrapper(this, "categories.yml");

        // Call onEnable() on the server platform
        VeinMinerServer.getInstance().onEnable();

        // Everything below this point is exclusive to Bukkit servers

        // Enable anti cheat hooks if required
        PluginManager manager = Bukkit.getPluginManager();
        this.registerAntiCheatHookIfEnabled(manager, "AAC5", AntiCheatHookAAC::new);
        this.registerAntiCheatHookIfEnabled(manager, "AntiAura", () -> new AntiCheatHookAntiAura(this));
        this.registerAntiCheatHookIfEnabled(manager, "LightAntiCheat", () -> new AntiCheatHookLightAntiCheat(this));
        this.registerAntiCheatHookIfEnabled(manager, "Matrix", AntiCheatHookMatrix::new);
        this.registerAntiCheatHookIfEnabled(manager, "NoCheatPlus", () -> new AntiCheatHookNCP(this));
        this.registerAntiCheatHookIfEnabled(manager, "Spartan", () -> new AntiCheatHookSpartan(this));

        // Register events
        this.getLogger().info("Registering events");
        manager.registerEvents(new BreakBlockListener(this), this);
        manager.registerEvents(new ItemCollectionListener(this), this);
        manager.registerEvents(new PlayerDataListener(this), this);

        Plugin mcMMOPlugin = manager.getPlugin("mcMMO");
        if (mcMMOPlugin != null && manager.isPluginEnabled("mcMMO")) {
            // Integrate with McMMO, but don't integrate with mcMMO-Classic, version 1.x
            if (!mcMMOPlugin.getDescription().getVersion().startsWith("1")) {
                manager.registerEvents(new McMMOIntegrationListener(this), this);
            }
            else if (getConfig().getBoolean(VMConstants.CONFIG_NERF_MCMMO, false)) {
                this.getLogger().warning(VMConstants.CONFIG_NERF_MCMMO + " is enabled but McMMO-Classic is installed.");
                this.getLogger().warning("This version of McMMO is not supported and therefore this configuration option will not work!");
                this.getLogger().warning("Consider updating your version of McMMO.");
            }
        }

        Plugin placeholderAPIPlugin = manager.getPlugin("PlaceholderAPI");
        if (placeholderAPIPlugin != null && manager.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderExpansionVeinMiner(this).register();
        }

        VeinMinerServer veinMiner = VeinMinerServer.getInstance();

        // Register commands
        this.getLogger().info("Registering commands");

        this.registerCommand("blocklist", new CommandBlocklist(this));
        this.registerCommand("toollist", new CommandToollist(this));
        this.registerCommand("veinminer", new CommandVeinMiner(this, getCommandOrThrow("blocklist"), getCommandOrThrow("toollist")));

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.getLogger().info("Vault found. Attempting to enable economy support...");

            SimpleVaultEconomy economy = new SimpleVaultEconomy();
            veinMiner.setEconomy(economy);

            this.getLogger().info(economy.hasEconomyPlugin() ? "Economy found! Hooked successfully." : "Cancelled. No economy plugin found.");
        } else {
            this.getLogger().info("Vault not found. Economy support suspended");
        }

        // Metrics
        if (getConfig().getBoolean(VMConstants.CONFIG_METRICS_ENABLED, true)) {
            this.getLogger().info("Enabling Plugin Metrics");

            Metrics metrics = new Metrics(this, 1938); // https://bstats.org/what-is-my-plugin-id
            metrics.addCustomChart(new AdvancedPie("blocks_veinmined", StatTracker::getVeinMinedCountAsData));
            metrics.addCustomChart(new SingleLineChart("using_client_mod", veinMiner.getPlayerManager()::getPlayerCountUsingClientMod));
            metrics.addCustomChart(new DrilldownPie("installed_anticheats", StatTracker::getInstalledAntiCheatsAsData));

            this.getLogger().info("Thanks for enabling Metrics! The anonymous stats are appreciated");
        }

        // Update check (https://www.spigotmc.org/resources/veinminer.12038/)
        UpdateChecker updateChecker = UpdateChecker.init(this, 12038);
        if (getConfig().getBoolean(VMConstants.CONFIG_PERFORM_UPDATE_CHECKS, true)) {
            this.getLogger().info("Performing an update check!");
            updateChecker.requestUpdateCheck().whenComplete((result, exception) -> {
                if (result.isUpdateAvailable()) {
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
        VeinMinerServer.getInstance().onDisable();
        this.anticheatHooks.clear();
    }

    /**
     * Get an instance of the main VeinMiner class (for VeinMiner API).
     *
     * @return an instance of the VeinMiner class
     */
    @NotNull
    public static VeinMinerPlugin getInstance() {
        return instance;
    }

    /**
     * Get the {@link VeinMinerManager}.
     *
     * @return the vein miner manager
     */
    @NotNull
    public VeinMinerManager getVeinMinerManager() {
        return VeinMinerServer.getInstance().getVeinMinerManager();
    }

    /**
     * Get the {@link ToolCategoryRegistry}.
     *
     * @return the tool category registry
     */
    @NotNull
    public ToolCategoryRegistry getToolCategoryRegistry() {
        return VeinMinerServer.getInstance().getToolCategoryRegistry();
    }

    /**
     * Get the {@link PatternRegistry}.
     *
     * @return the pattern registry
     */
    @NotNull
    public PatternRegistry getPatternRegistry() {
        return VeinMinerServer.getInstance().getPatternRegistry();
    }

    /**
     * Get the {@link VeinMinerPlayerManager} instance.
     *
     * @return the player manager
     */
    @NotNull
    public VeinMinerPlayerManager getPlayerManager() {
        return VeinMinerServer.getInstance().getPlayerManager();
    }

    /**
     * Get the default {@link VeinMiningPattern} to be used for new players.
     *
     * @return the default vein mining pattern
     */
    @NotNull
    public VeinMiningPattern getDefaultVeinMiningPattern() {
        return VeinMinerServer.getInstance().getDefaultVeinMiningPattern();
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
     * Register an anti cheat hook to VeinMiner. Hooks should be registered for all anti cheat plugins
     * as to support VeinMining and not false-flag players with fast-break.
     *
     * @param hook the hook to register
     *
     * @return true if registered, false if unsupported
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
     * Get an immutable list of all anti cheat hooks.
     *
     * @return all anti cheat hooks
     */
    @NotNull
    public List<AntiCheatHook> getAnticheatHooks() {
        return Collections.unmodifiableList(anticheatHooks);
    }

    /**
     * Get a {@link NamespacedKey} with VeinMiner's namespace.
     *
     * @param key the key
     *
     * @return a VeinMiner namespaced key
     */
    @NotNull
    public static NamespacedKey key(String key) {
        return new NamespacedKey(instance, key);
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
            StatTracker.recognizeInstalledAntiCheat(new AntiCheat(antiCheatPlugin.getName(), antiCheatPlugin.getDescription().getVersion()));
        }
    }

    private void registerCommand(@NotNull String commandName, @NotNull CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }

    private PluginCommand getCommandOrThrow(@NotNull String commandName) {
        PluginCommand command = getCommand(commandName);

        if (command == null) {
            throw new IllegalStateException("Missing command: " + commandName);
        }

        return command;
    }

}
