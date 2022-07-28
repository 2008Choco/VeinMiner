package wtf.choco.veinminer;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.data.PersistentDataStorageNoOp;
import wtf.choco.veinminer.economy.EmptyEconomy;
import wtf.choco.veinminer.economy.SimpleEconomy;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.manager.VeinMinerPlayerManager;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.pattern.VeinMiningPatternDefault;
import wtf.choco.veinminer.platform.ServerPlatform;
import wtf.choco.veinminer.tool.ToolCategoryRegistry;

/**
 * A class holding VeinMiner's core common functionality.
 */
public final class VeinMinerServer implements VeinMiner {

    private static VeinMinerServer instance;

    private ActivationStrategy defaultActivationStrategy = ActivationStrategy.SNEAK;
    private VeinMiningPattern defaultVeinMiningPattern = VeinMiningPatternDefault.getInstance();

    private VeinMinerManager veinMinerManager = new VeinMinerManager();
    private VeinMinerPlayerManager playerManager = new VeinMinerPlayerManager();
    private ToolCategoryRegistry toolCategoryRegistry = new ToolCategoryRegistry();
    private PatternRegistry patternRegistry = new PatternRegistry();

    private PersistentDataStorage persistentDataStorage = PersistentDataStorageNoOp.INSTANCE;

    private SimpleEconomy economy = EmptyEconomy.INSTANCE;

    private ServerPlatform platform;

    private VeinMinerServer() { }

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
     * Set the implementation of {@link ServerPlatform}.
     *
     * @param platform the instance to set
     *
     * @throws IllegalStateException if the platform has already been set
     */
    public void setPlatform(@NotNull ServerPlatform platform) {
        if (this.platform != null) {
            throw new IllegalStateException("platform has already been set");
        }

        this.platform = platform;
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
        return getPlatform().getVeinMinerVersion();
    }

    @Override
    public boolean isServer() {
        return true;
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
