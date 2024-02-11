package wtf.choco.veinminer;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.bukkit.BukkitProtocolConfiguration;
import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.anticheat.AntiCheatHookAAC;
import wtf.choco.veinminer.anticheat.AntiCheatHookAntiAura;
import wtf.choco.veinminer.anticheat.AntiCheatHookGrim;
import wtf.choco.veinminer.anticheat.AntiCheatHookLightAntiCheat;
import wtf.choco.veinminer.anticheat.AntiCheatHookMatrix;
import wtf.choco.veinminer.anticheat.AntiCheatHookNCP;
import wtf.choco.veinminer.anticheat.AntiCheatHookSpartan;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.command.CommandBlocklist;
import wtf.choco.veinminer.command.CommandToollist;
import wtf.choco.veinminer.command.CommandVeinMiner;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.config.ConfigWrapper;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.config.impl.StandardVeinMinerConfiguration;
import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.data.PersistentDataStorageJSON;
import wtf.choco.veinminer.data.PersistentDataStorageMySQL;
import wtf.choco.veinminer.data.PersistentDataStorageNoOp;
import wtf.choco.veinminer.data.PersistentDataStorageSQLite;
import wtf.choco.veinminer.economy.EmptyEconomy;
import wtf.choco.veinminer.economy.SimpleEconomy;
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
import wtf.choco.veinminer.network.VeinMinerBukkitChannelRegistrar;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPatternDefault;
import wtf.choco.veinminer.pattern.VeinMiningPatternStaircase;
import wtf.choco.veinminer.pattern.VeinMiningPatternStaircase.Direction;
import wtf.choco.veinminer.pattern.VeinMiningPatternTunnel;
import wtf.choco.veinminer.tool.ToolCategoryRegistry;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.tool.VeinMinerToolCategoryHand;
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
    private ToolCategoryRegistry toolCategoryRegistry = new ToolCategoryRegistry();
    private PatternRegistry patternRegistry = new PatternRegistry();

    private SimpleEconomy economy = EmptyEconomy.INSTANCE;
    private PersistentDataStorage storage = PersistentDataStorageNoOp.INSTANCE;

    private final UpdateChecker updateChecker = new SpigotMCUpdateChecker(this, 12038);
    private final List<AntiCheatHook> anticheatHooks = new ArrayList<>();

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

        this.saveDefaultConfig();

        // Persistent storage
        this.setupPersistentStorage();

        // VeinMinerManager and ToolCategory loading into memory
        this.getLogger().info("Loading configuration options to local memory");
        this.reloadVeinMinerManagerConfig();
        this.reloadToolCategoryRegistryConfig();

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
                            this.getLogger().info("Your version of VeinMiner is out of date. Version %s is available for download.".formatted(result.getNewestVersion()));
                        } else {
                            this.getLogger().info("You are on the latest version of VeinMiner.");
                        }
                    }
                );
            });
        }

        // Everything below this point is exclusive to Bukkit servers

        // Enable anti cheat hooks if required
        PluginManager manager = Bukkit.getPluginManager();
        this.registerAntiCheatHookIfEnabled(manager, "AAC5", AntiCheatHookAAC::new);
        this.registerAntiCheatHookIfEnabled(manager, "AntiAura", () -> new AntiCheatHookAntiAura(this));
        this.registerAntiCheatHookIfEnabled(manager, "GrimAC", AntiCheatHookGrim::new);
        this.registerAntiCheatHookIfEnabled(manager, "LightAntiCheat", () -> new AntiCheatHookLightAntiCheat(this));
        this.registerAntiCheatHookIfEnabled(manager, "Matrix", AntiCheatHookMatrix::new);
        this.registerAntiCheatHookIfEnabled(manager, "NoCheatPlus", () -> new AntiCheatHookNCP(this));
        this.registerAntiCheatHookIfEnabled(manager, "Spartan", () -> new AntiCheatHookSpartan(this));

        // Register commands
        this.getLogger().info("Registering commands");

        TabExecutor blocklistCommand = new CommandBlocklist(this), toollistCommand = new CommandToollist(this);
        this.registerCommand("blocklist", blocklistCommand);
        this.registerCommand("toollist", toollistCommand);
        this.registerCommand("veinminer", new CommandVeinMiner(this, blocklistCommand, toollistCommand));

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

            SimpleVaultEconomy economy = new SimpleVaultEconomy();
            this.setEconomy(economy);

            this.getLogger().info(economy.hasEconomyPlugin() ? "Economy found! Hooked successfully." : "Cancelled. No economy plugin found.");
        } else {
            this.getLogger().info("Vault not found. Economy support suspended");
        }

        // Metrics
        if (getConfiguration().isMetricsEnabled()) {
            this.getLogger().info("Enabling Plugin Metrics");

            Metrics metrics = new Metrics(this, 1938); // https://bstats.org/what-is-my-plugin-id
            metrics.addCustomChart(new AdvancedPie("blocks_veinmined", StatTracker::getVeinMinedCountAsData));
            metrics.addCustomChart(new SingleLineChart("using_client_mod", playerManager::getPlayerCountUsingClientMod));
            metrics.addCustomChart(new DrilldownPie("installed_anticheats", StatTracker::getInstalledAntiCheatsAsData));

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
        return veinMinerManager;
    }

    /**
     * Get the {@link ToolCategoryRegistry}.
     *
     * @return the tool category registry
     */
    @NotNull
    public ToolCategoryRegistry getToolCategoryRegistry() {
        return toolCategoryRegistry;
    }

    /**
     * Get the {@link PatternRegistry}.
     *
     * @return the pattern registry
     */
    @NotNull
    public PatternRegistry getPatternRegistry() {
        return patternRegistry;
    }

    /**
     * Get the {@link VeinMinerPlayerManager} instance.
     *
     * @return the player manager
     */
    @NotNull
    public VeinMinerPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Set the {@link SimpleEconomy} implementation.
     *
     * @param economy the economy
     */
    public void setEconomy(@NotNull SimpleEconomy economy) {
        this.economy = economy;
    }

    /**
     * Get the {@link SimpleEconomy}.
     *
     * @return the economy
     */
    @NotNull
    public SimpleEconomy getEconomy() {
        return economy;
    }

    /**
     * Set the {@link PersistentDataStorage} for the server.
     *
     * @param storage the persistent data storage to set
     */
    public void setPersistentDataStorage(@NotNull PersistentDataStorage storage) {
        this.storage = storage;
    }

    /**
     * Get the {@link PersistentDataStorage} for the server.
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
     * Get an instance of the categories configuration file.
     *
     * @return the categories config
     */
    @NotNull
    public ConfigWrapper getCategoriesConfig() {
        return categoriesConfig;
    }

    @NotNull
    public VeinMinerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Create a {@link ClientConfig} with values supplied by the plugin's config.
     *
     * @param player the player for whom to create the config
     *
     * @return the default client config
     */
    @NotNull
    public ClientConfig createClientConfig(@NotNull Player player) {
        ClientConfig defaultConfig = getConfiguration().getClientConfiguration();

        return ClientConfig.builder()
                .allowActivationKeybind(defaultConfig.isAllowActivationKeybind() && player.hasPermission("veinminer.client.activation"))
                .allowPatternSwitchingKeybind(defaultConfig.isAllowPatternSwitchingKeybind() && player.hasPermission("veinminer.client.patterns"))
                .allowWireframeRendering(defaultConfig.isAllowWireframeRendering() && player.hasPermission("veinminer.client.wireframe"))
                .build();
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
            StatTracker.recognizeInstalledAntiCheat(new AntiCheat(antiCheatPlugin.getName(), antiCheatPlugin.getDescription().getVersion()));
        }
    }

    /**
     * Reload the {@link VeinMinerManager}'s values from config into memory.
     */
    public void reloadVeinMinerManagerConfig() {
        this.veinMinerManager.clear();

        // Global block list and disabled game modes
        this.veinMinerManager.setGlobalBlockList(BlockList.parseBlockList(getConfiguration().getGlobalBlockListKeys(), getLogger()));

        // Aliases
        int aliasesAdded = 0;
        for (String aliasString : getConfiguration().getAliasStrings()) {
            List<String> aliasStringEntries = List.of(aliasString.split(";"));
            if (aliasStringEntries.size() <= 1) {
                this.getLogger().info("Alias \"%s\" contains %d entries but must have at least 2. Not adding.".formatted(aliasString, aliasStringEntries.size()));
                continue;
            }

            BlockList aliasBlockList = BlockList.parseBlockList(aliasStringEntries, getLogger());
            if (aliasBlockList.isEmpty()) {
                continue;
            }

            this.getVeinMinerManager().addAlias(aliasBlockList);
            aliasesAdded++;
        }

        this.getLogger().info("Added " + aliasesAdded + " aliases.");
    }

    /**
     * Reload the {@link ToolCategoryRegistry}'s values from config into memory.
     */
    public void reloadToolCategoryRegistryConfig() {
        this.toolCategoryRegistry.unregisterAll(); // Unregister all the categories before re-loading them

        for (String categoryId : getConfiguration().getDefinedCategoryIds()) {
            if (categoryId.contains(" ")) {
                this.getLogger().info(String.format("Category id \"%s\" is invalid. Must not contain spaces (' ')", categoryId));
                continue;
            }

            if (categoryId.equalsIgnoreCase("Hand")) {
                this.getLogger().warning("Redefinition of the Hand category is not legal. Ignoring.");
                continue;
            }

            ToolCategoryConfiguration config = getConfiguration().getToolCategoryConfiguration(categoryId);
            if (config == null) {
                // Should be impossible, but we'll double check
                continue;
            }

            Set<Material> items = new HashSet<>();
            for (String itemTypeString : config.getItemKeys()) {
                Material material = Material.matchMaterial(itemTypeString);

                if (material == null || !material.isItem()) {
                    this.getLogger().info(String.format("Unknown item for input \"%s\". Did you spell it correctly?", itemTypeString));
                    continue;
                }

                items.add(material);
            }

            if (items.isEmpty()) {
                this.getLogger().info(String.format("Category with id \"%s\" has no items. Ignoring registration.", categoryId));
                continue;
            }

            Collection<String> blockStateStrings = getConfiguration().getBlockListKeys(categoryId);
            if (blockStateStrings.isEmpty()) {
                this.getLogger().info(String.format("No block list configured for category with id \"%s\". Ignoring registration.", categoryId));
                continue;
            }

            BlockList blocklist = BlockList.parseBlockList(blockStateStrings, getLogger());
            if (blocklist.size() == 0) {
                this.getLogger().info(String.format("No block list configured for category with id \"%s\". Ignoring registration.", categoryId));
                continue;
            }

            int priority = config.getPriority();
            String nbtValue = config.getNBTValue();

            this.toolCategoryRegistry.register(new VeinMinerToolCategory(categoryId, priority, nbtValue, blocklist, config, items));
            this.getLogger().info(String.format("Registered category with id \"%s\" holding %d unique items and %d unique blocks.", categoryId, items.size(), blocklist.size()));
        }

        // Also register the hand category (required)
        getToolCategoryRegistry().register(new VeinMinerToolCategoryHand(BlockList.parseBlockList(getConfiguration().getBlockListKeys("Hand"), getLogger()), getConfiguration()));

        // Register permissions dynamically
        Permission veinminePermissionParent = getOrRegisterPermission("veinminer.veinmine.*", () -> "Allow the use of vein miner for all tool categories", PermissionDefault.TRUE);
        Permission blocklistPermissionParent = getOrRegisterPermission("veinminer.blocklist.list.*", () -> "Allow access to list the blocks in all block lists", PermissionDefault.OP);
        Permission toollistPermissionParent = getOrRegisterPermission("veinminer.toollist.list.*", () -> "Allow access to list the tools in a category's tool list", PermissionDefault.OP);

        for (VeinMinerToolCategory category : getToolCategoryRegistry().getAll()) {
            String id = category.getId().toLowerCase();

            Permission veinminePermission = getOrRegisterPermission("veinminer.veinmine." + id, () -> "Allows players to vein mine using the " + category.getId() + " category", PermissionDefault.TRUE);
            Permission blocklistPermission = getOrRegisterPermission("veinminer.blocklist.list." + id, () -> "Allows players to list blocks in the " + category.getId() + " category", PermissionDefault.OP);
            Permission toollistPermission = getOrRegisterPermission("veinminer.toollist.list." + id, () -> "Allows players to list tools in the " + category.getId() + " category", PermissionDefault.OP);

            veinminePermissionParent.getChildren().put(veinminePermission.getName(), true);
            blocklistPermissionParent.getChildren().put(blocklistPermission.getName(), true);
            toollistPermissionParent.getChildren().put(toollistPermission.getName(), true);
        }

        veinminePermissionParent.recalculatePermissibles();
        blocklistPermissionParent.recalculatePermissibles();
        toollistPermissionParent.recalculatePermissibles();
    }

    private void setupPersistentStorage() {
        PersistentDataStorage.Type storageType = getConfiguration().getStorageType();

        try {
            PersistentDataStorage persistentDataStorage = switch (storageType) {
                case JSON -> {
                    File jsonDirectory = getConfiguration().getJsonStorageDirectory();

                    if (jsonDirectory == null) {
                        this.getLogger().warning("Incomplete configuration for JSON persistent storage. Requires a valid directory.");
                        yield PersistentDataStorageNoOp.INSTANCE;
                    }

                    yield new PersistentDataStorageJSON(this, jsonDirectory);
                }
                case SQLITE -> new PersistentDataStorageSQLite(this, getDataFolder().toPath(), "veinminer.db");
                case MYSQL -> {
                    String host = getConfiguration().getMySQLHost();
                    int port = getConfiguration().getMySQLPort();
                    String username = getConfiguration().getMySQLUsername();
                    String password = getConfiguration().getMySQLPassword();
                    String database = getConfiguration().getMySQLDatabase();
                    String tablePrefix = getConfiguration().getMySQLTablePrefix();

                    if (host == null || database == null || username == null || password == null || tablePrefix == null) {
                        this.getLogger().warning("Incomplete configuration for MySQL persistent storage. Requires a valid host, port, database, username, password, and table prefix.");
                        yield PersistentDataStorageNoOp.INSTANCE;
                    }

                    yield new PersistentDataStorageMySQL(this, host, port, username, password, database, tablePrefix);
                }
                default -> {
                    this.getLogger().warning("No persistent storage is available. This may be a bug.");
                    yield PersistentDataStorageNoOp.INSTANCE;
                }
            };

            this.setPersistentDataStorage(persistentDataStorage);

            persistentDataStorage.init().whenComplete((result, e) -> {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                this.getLogger().info("Using " + persistentDataStorage.getType() + " for persistent storage.");
            });
        } catch (IOException e) {
            this.getLogger().severe("Could not setup persistent file storage. Player data cannot be saved nor loaded. Investigate IMMEDIATELY.");
            e.printStackTrace();
        }
    }

    private Permission getOrRegisterPermission(String permissionName, Supplier<String> description, PermissionDefault permissionDefault) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Permission permission = pluginManager.getPermission(permissionName);

        if (permission == null) {
            permission = new Permission(permissionName, description.get(), permissionDefault);
            pluginManager.addPermission(permission);
        }

        return permission;
    }

}
