package wtf.choco.veinminer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.command.Command;
import wtf.choco.veinminer.command.CommandBlocklist;
import wtf.choco.veinminer.command.CommandToollist;
import wtf.choco.veinminer.command.CommandVeinMiner;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.config.VeinMiningConfig;
import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.data.PersistentDataStorageJSON;
import wtf.choco.veinminer.data.PersistentDataStorageMySQL;
import wtf.choco.veinminer.data.PersistentDataStorageNoOp;
import wtf.choco.veinminer.data.PersistentDataStorageSQLite;
import wtf.choco.veinminer.economy.EmptyEconomy;
import wtf.choco.veinminer.economy.SimpleEconomy;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.manager.VeinMinerPlayerManager;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.pattern.VeinMiningPatternDefault;
import wtf.choco.veinminer.pattern.VeinMiningPatternStaircase;
import wtf.choco.veinminer.pattern.VeinMiningPatternStaircase.Direction;
import wtf.choco.veinminer.pattern.VeinMiningPatternTunnel;
import wtf.choco.veinminer.platform.GameMode;
import wtf.choco.veinminer.platform.PlatformPermission;
import wtf.choco.veinminer.platform.PlatformPlayer;
import wtf.choco.veinminer.platform.ServerPlatform;
import wtf.choco.veinminer.platform.world.ItemType;
import wtf.choco.veinminer.tool.ToolCategoryRegistry;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.tool.VeinMinerToolCategoryHand;
import wtf.choco.veinminer.update.StandardVersionSchemes;
import wtf.choco.veinminer.update.UpdateChecker;

/**
 * A class holding VeinMiner's core common functionality.
 */
public final class VeinMinerServer implements VeinMiner {

    private static VeinMinerServer instance;

    private ActivationStrategy defaultActivationStrategy = ActivationStrategy.SNEAK;
    private VeinMiningPattern defaultVeinMiningPattern = VeinMiningPatternDefault.getInstance();

    private VeinMinerManager veinMinerManager = new VeinMinerManager(this);
    private VeinMinerPlayerManager playerManager = new VeinMinerPlayerManager();
    private ToolCategoryRegistry toolCategoryRegistry = new ToolCategoryRegistry();
    private PatternRegistry patternRegistry = new PatternRegistry();

    private PersistentDataStorage persistentDataStorage = PersistentDataStorageNoOp.INSTANCE;

    private SimpleEconomy economy = EmptyEconomy.INSTANCE;

    private ServerPlatform platform;

    private VeinMinerServer() { }

    /**
     * Called when the server plugin loads.
     *
     * @param platform the server platform implementation to set
     */
    public void onLoad(@NotNull ServerPlatform platform) {
        this.platform = platform;

        // Register all default patterns
        this.patternRegistry.register(VeinMiningPatternDefault.getInstance());
        this.patternRegistry.register(new VeinMiningPatternTunnel());
        this.patternRegistry.register(new VeinMiningPatternStaircase(Direction.UP));
        this.patternRegistry.register(new VeinMiningPatternStaircase(Direction.DOWN));
    }

    /**
     * Called when the server plugin enables.
     */
    public void onEnable() {
        this.platform.getConfig().saveDefaults();

        // Persistent storage
        this.setupPersistentStorage();

        // VeinMinerManager and ToolCategory loading into memory
        this.platform.getLogger().info("Loading configuration options to local memory");
        this.reloadVeinMinerManagerConfig();
        this.reloadToolCategoryRegistryConfig();

        // Register commands
        this.platform.getLogger().info("Registering commands");

        Command blocklistCommand = new CommandBlocklist(this), toollistCommand = new CommandToollist(this);
        this.platform.registerCommand("blocklist", blocklistCommand);
        this.platform.registerCommand("toollist", toollistCommand);
        this.platform.registerCommand("veinminer", new CommandVeinMiner(instance, blocklistCommand, toollistCommand));

        // Special case for server reloads
        this.persistentDataStorage.load(platform.getOnlinePlayers().stream().map(playerManager::get).toList());

        // Update check
        UpdateChecker updateChecker = platform.getUpdateChecker();
        if (platform.getConfig().shouldCheckForUpdates()) {
            Logger logger = platform.getLogger();
            logger.info("Performing an update check!");

            updateChecker.checkForUpdates(StandardVersionSchemes.DECIMAL).thenAccept(result -> {
                result.getException().ifPresentOrElse(
                    e -> {
                        logger.info("Could not check for an update. Reason: ".formatted(e.getMessage()));
                    },
                    () -> {
                        if (result.isUpdateAvailable()) {
                            logger.info("Your version of VeinMiner is out of date. Version %s is available for download.".formatted(result.getNewestVersion()));
                        } else {
                            logger.info("You are on the latest version of VeinMiner.");
                        }
                    }
                );
            });
        }
    }

    /**
     * Called when the server plugin disables.
     */
    public void onDisable() {
        this.platform.getLogger().info("Clearing localized data");
        this.getVeinMinerManager().clear();

        this.getPatternRegistry().unregisterAll();
        this.getToolCategoryRegistry().unregisterAll();

        this.persistentDataStorage.save(playerManager.getAll());
    }

    /**
     * Set the default {@link ActivationStrategy} to use for players that have not explicitly
     * set one.
     *
     * @param activationStrategy the activation strategy to set
     */
    public void setDefaultActivationStrategy(@NotNull ActivationStrategy activationStrategy) {
        this.defaultActivationStrategy = activationStrategy;
    }

    /**
     * Get the default {@link ActivationStrategy} to use for players that have not explicitly
     * set one.
     *
     * @return the default activation strategy
     */
    @NotNull
    public ActivationStrategy getDefaultActivationStrategy() {
        return defaultActivationStrategy;
    }

    /**
     * Set the default {@link VeinMiningPattern} to use for players that have not explicitly
     * set one.
     *
     * @param pattern the pattern to set
     */
    @NotNull
    public void setDefaultVeinMiningPattern(@NotNull VeinMiningPattern pattern) {
        this.defaultVeinMiningPattern = pattern;
    }

    /**
     * Get the default {@link VeinMiningPattern} to use for players that have not explicitly
     * set one.
     *
     * @return the default pattern
     */
    @NotNull
    public VeinMiningPattern getDefaultVeinMiningPattern() {
        return defaultVeinMiningPattern;
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
     * Get the {@link VeinMinerPlayerManager}.
     *
     * @return the player manager
     */
    @NotNull
    public VeinMinerPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Get the {@link ToolCategoryRegistry}.
     *
     * @return the tool category registry
     */
    @NotNull
    public ToolCategoryRegistry getToolCategoryRegistry() {
        if (toolCategoryRegistry == null) {
            throw new IllegalStateException("toolCategoryRegistry has not been set.");
        }

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
     * Set the {@link PersistentDataStorage} for the server.
     *
     * @param persistentDataStorage the persistent data storage to set
     */
    public void setPersistentDataStorage(@NotNull PersistentDataStorage persistentDataStorage) {
        this.persistentDataStorage = persistentDataStorage;
    }

    /**
     * Get the {@link PersistentDataStorage} for the server.
     *
     * @return the persistent data storage
     */
    @NotNull
    public PersistentDataStorage getPersistentDataStorage() {
        return persistentDataStorage;
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
     * Get the {@link ServerPlatform} instance.
     *
     * @return the platform instance
     */
    @NotNull
    public ServerPlatform getPlatform() {
        if (toolCategoryRegistry == null) {
            throw new IllegalStateException("platform has not been set.");
        }

        return platform;
    }

    @NotNull
    @Override
    public String getVersion() {
        return getPlatform().getVeinMinerDetails().version();
    }

    @Override
    public boolean isServer() {
        return true;
    }

    /**
     * Create a {@link VeinMiningConfig} with values supplied by the plugin's config.
     *
     * @param player the player for whom to create the config
     *
     * @return the default client config
     */
    @NotNull
    public ClientConfig createClientConfig(@NotNull PlatformPlayer player) {
        ClientConfig defaultConfig = platform.getConfig().getClientConfiguration();

        return ClientConfig.builder()
                .allowActivationKeybind(defaultConfig.isAllowActivationKeybind() && player.hasPermission("veinminer.client.activation"))
                .allowPatternSwitchingKeybind(defaultConfig.isAllowPatternSwitchingKeybind() && player.hasPermission("veinminer.client.patterns"))
                .allowWireframeRendering(defaultConfig.isAllowWireframeRendering() && player.hasPermission("veinminer.client.wireframe"))
                .build();
    }

    /**
     * Reload the {@link VeinMinerManager}'s values from config into memory.
     */
    public void reloadVeinMinerManagerConfig() {
        this.veinMinerManager.clear();
        VeinMinerConfiguration config = platform.getConfig();

        // Default activation strategy
        this.setDefaultActivationStrategy(config.getDefaultActivationStrategy());
        this.setDefaultVeinMiningPattern(config.getDefaultVeinMiningPattern());

        // Global block list and config
        this.veinMinerManager.setGlobalBlockList(BlockList.parseBlockList(config.getGlobalBlockList(), platform.getLogger()));
        this.veinMinerManager.setGlobalConfig(VeinMiningConfig.builder()
                .repairFriendly(config.isRepairFriendly())
                .maxVeinSize(config.getMaxVeinSize())
                .cost(config.getCost())
                .disableWorlds(config.getDisabledWorlds())
                .build()
        );

        // Disabled game modes
        Set<GameMode> disabledGameModes = new HashSet<>();
        for (String disabledGameModeName : config.getDisabledGameModeNames()) {
            GameMode gameMode = GameMode.getById(disabledGameModeName);

            if (gameMode == null) {
                this.platform.getLogger().info("Unrecognized game mode for input \"%s\". Did you spell it correctly?".formatted(disabledGameModeName));
                continue;
            }

            disabledGameModes.add(gameMode);
        }

        this.veinMinerManager.setDisabledGameModes(disabledGameModes);

        // Aliases
        int aliasesAdded = 0;
        for (String aliasString : config.getRawAliasStrings()) {
            List<String> aliasStringEntries = List.of(aliasString.split(";"));
            if (aliasStringEntries.size() <= 1) {
                this.platform.getLogger().info("Alias \"%s\" contains %d entries but must have at least 2. Not adding.".formatted(aliasString, aliasStringEntries.size()));
                continue;
            }

            BlockList aliasBlockList = BlockList.parseBlockList(aliasStringEntries, platform.getLogger());
            if (aliasBlockList.isEmpty()) {
                continue;
            }

            this.getVeinMinerManager().addAlias(aliasBlockList);
            aliasesAdded++;
        }

        this.platform.getLogger().info("Added " + aliasesAdded + " aliases.");
    }

    /**
     * Reload the {@link ToolCategoryRegistry}'s values from config into memory.
     */
    public void reloadToolCategoryRegistryConfig() {
        this.toolCategoryRegistry.unregisterAll(); // Unregister all the categories before re-loading them

        VeinMinerConfiguration config = platform.getConfig();
        for (String categoryId : config.getAllDefinedCategoryIds()) {
            if (categoryId.contains(" ")) {
                this.platform.getLogger().info(String.format("Category id \"%s\" is invalid. Must not contain spaces (' ')", categoryId));
                continue;
            }

            if (categoryId.equalsIgnoreCase("Hand")) {
                this.platform.getLogger().warning("Redefinition of the Hand category is not legal. Ignoring.");
                continue;
            }

            VeinMiningConfig globalConfig = veinMinerManager.getGlobalConfig();
            VeinMiningConfig veinMinerConfig = VeinMiningConfig.builder()
                    .repairFriendly(config.isRepairFriendly(categoryId, globalConfig.isRepairFriendly()))
                    .maxVeinSize(config.getMaxVeinSize(categoryId, globalConfig.getMaxVeinSize()))
                    .cost(config.getCost(categoryId, globalConfig.getCost()))
                    .disableWorlds(config.getDisabledWorlds(categoryId, () -> globalConfig.getDisabledWorlds()))
                    .build();

            Set<ItemType> items = new HashSet<>();
            for (String itemTypeString : config.getCategoryItemList(categoryId)) {
                ItemType itemType = platform.getItemType(itemTypeString);

                if (itemType == null) {
                    this.platform.getLogger().info(String.format("Unknown item for input \"%s\". Did you spell it correctly?", itemTypeString));
                    continue;
                }

                items.add(itemType);
            }

            if (items.isEmpty()) {
                this.platform.getLogger().info(String.format("Category with id \"%s\" has no items. Ignoring registration.", categoryId));
                continue;
            }

            List<String> blockStateStrings = config.getCategoryBlockList(categoryId);
            if (blockStateStrings.isEmpty()) {
                this.platform.getLogger().info(String.format("No block list configured for category with id \"%s\". Ignoring registration.", categoryId));
                continue;
            }

            BlockList blocklist = BlockList.parseBlockList(blockStateStrings, platform.getLogger());
            if (blocklist.size() == 0) {
                this.platform.getLogger().info(String.format("No block list configured for category with id \"%s\". Ignoring registration.", categoryId));
                continue;
            }

            int priority = config.getPriority(categoryId);
            String nbtValue = config.getNBTValue(categoryId);

            this.toolCategoryRegistry.register(new VeinMinerToolCategory(categoryId, priority, nbtValue, blocklist, veinMinerConfig, items));
            this.platform.getLogger().info(String.format("Registered category with id \"%s\" holding %d unique items and %d unique blocks.", categoryId, items.size(), blocklist.size()));
        }

        // Also register the hand category (required)
        getToolCategoryRegistry().register(new VeinMinerToolCategoryHand(BlockList.parseBlockList(config.getCategoryBlockList("Hand"), platform.getLogger()), getVeinMinerManager().getGlobalConfig()));

        // Register permissions dynamically
        PlatformPermission veinminePermissionParent = platform.getOrRegisterPermission("veinminer.veinmine.*", () -> "Allow the use of vein miner for all tool categories", PlatformPermission.Default.TRUE);
        PlatformPermission blocklistPermissionParent = platform.getOrRegisterPermission("veinminer.blocklist.list.*", () -> "Allow access to list the blocks in all block lists", PlatformPermission.Default.OP);
        PlatformPermission toollistPermissionParent = platform.getOrRegisterPermission("veinminer.toollist.list.*", () -> "Allow access to list the tools in a category's tool list", PlatformPermission.Default.OP);

        for (VeinMinerToolCategory category : getToolCategoryRegistry().getAll()) {
            String id = category.getId().toLowerCase();

            PlatformPermission veinminePermission = platform.getOrRegisterPermission("veinminer.veinmine." + id, () -> "Allows players to vein mine using the " + category.getId() + " category", PlatformPermission.Default.TRUE);
            PlatformPermission blocklistPermission = platform.getOrRegisterPermission("veinminer.blocklist.list." + id, () -> "Allows players to list blocks in the " + category.getId() + " category", PlatformPermission.Default.OP);
            PlatformPermission toollistPermission = platform.getOrRegisterPermission("veinminer.toollist.list." + id, () -> "Allows players to list tools in the " + category.getId() + " category", PlatformPermission.Default.OP);

            veinminePermissionParent.addChild(veinminePermission, true);
            blocklistPermissionParent.addChild(blocklistPermission, true);
            toollistPermissionParent.addChild(toollistPermission, true);
        }

        veinminePermissionParent.recalculatePermissibles();
        blocklistPermissionParent.recalculatePermissibles();
        toollistPermissionParent.recalculatePermissibles();
    }

    private void setupPersistentStorage() {
        VeinMinerConfiguration config = platform.getConfig();
        PersistentDataStorage.Type storageType = config.getStorageType();

        try {
            PersistentDataStorage persistentDataStorage = switch (storageType) {
                case JSON -> {
                    File jsonDirectory = config.getJsonStorageDirectory();

                    if (jsonDirectory == null) {
                        this.platform.getLogger().warning("Incomplete configuration for JSON persistent storage. Requires a valid directory.");
                        yield PersistentDataStorageNoOp.INSTANCE;
                    }

                    yield new PersistentDataStorageJSON(this, jsonDirectory);
                }
                case SQLITE -> new PersistentDataStorageSQLite(this, platform.getVeinMinerPluginDirectory().toPath(), "veinminer.db");
                case MYSQL -> {
                    String host = config.getMySQLHost();
                    int port = config.getMySQLPort();
                    String username = config.getMySQLUsername();
                    String password = config.getMySQLPassword();
                    String database = config.getMySQLDatabase();
                    String tablePrefix = config.getMySQLTablePrefix();

                    if (host == null || database == null || username == null || password == null || tablePrefix == null) {
                        this.platform.getLogger().warning("Incomplete configuration for MySQL persistent storage. Requires a valid host, port, database, username, password, and table prefix.");
                        yield PersistentDataStorageNoOp.INSTANCE;
                    }

                    yield new PersistentDataStorageMySQL(this, host, port, username, password, database, tablePrefix);
                }
                default -> {
                    this.platform.getLogger().warning("No persistent storage is available. This may be a bug.");
                    yield PersistentDataStorageNoOp.INSTANCE;
                }
            };

            this.setPersistentDataStorage(persistentDataStorage);

            persistentDataStorage.init().whenComplete((result, e) -> {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                this.platform.getLogger().info("Using " + persistentDataStorage.getType() + " for persistent storage.");
            });
        } catch (IOException e) {
            this.platform.getLogger().severe("Could not setup persistent file storage. Player data cannot be saved nor loaded. Investigate IMMEDIATELY.");
            e.printStackTrace();
        }
    }

    /**
     * Get the {@link VeinMinerServer} instance.
     *
     * @return the vein miner instance
     */
    @NotNull
    public static VeinMinerServer getInstance() {
        return (instance != null) ? instance : (instance = new VeinMinerServer());
    }

}
