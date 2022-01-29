package wtf.choco.veinminer;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessageProtocol;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.platform.PlatformReconstructor;
import wtf.choco.veinminer.tool.ToolCategoryRegistry;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A class holding VeinMiner's core common functionality.
 */
public final class VeinMiner {

    /**
     * A pattern to match a Minecraft block state. e.g. {@code minecraft:chest[waterlogged=false]}
     * <ul>
     *   <li><strong>Group 1</strong>: The block's type
     *   <li><strong>Group 2</strong>: The block's states (without the [] brackets)
     * </ul>
     */
    public static final Pattern PATTERN_BLOCK_STATE = Pattern.compile("([a-z0-9:._-]+)(?:\\[(.+=.+)+\\])*");

    private static final NamespacedKey PROTOCOL_CHANNEL = NamespacedKey.veinminer("veinminer");
    private static final int PROTOCOL_VERSION = 1;

    /**
     * VeinMiner's messaging protocol.
     *
     * @see PluginMessageProtocol
     */
    public static final PluginMessageProtocol PROTOCOL = new PluginMessageProtocol(PROTOCOL_CHANNEL, PROTOCOL_VERSION,
            serverboundRegistry -> serverboundRegistry
                .registerMessage(PluginMessageServerboundHandshake.class, PluginMessageServerboundHandshake::new)
                .registerMessage(PluginMessageServerboundToggleVeinMiner.class, PluginMessageServerboundToggleVeinMiner::new)
                .registerMessage(PluginMessageServerboundRequestVeinMine.class, PluginMessageServerboundRequestVeinMine::new),

            clientboundRegistry -> clientboundRegistry
                .registerMessage(PluginMessageClientboundHandshakeResponse.class, PluginMessageClientboundHandshakeResponse::new)
                .registerMessage(PluginMessageClientboundVeinMineResults.class, PluginMessageClientboundVeinMineResults::new)
    );

    private static VeinMiner instance;

    private ActivationStrategy defaultActivationStrategy = ActivationStrategy.SNEAK;

    private ToolCategoryRegistry toolCategoryRegistry;
    private PlatformReconstructor platformReconstructor;

    private VeinMiner() { }

    /**
     * Get the default {@link ActivationStrategy} to use for players that have not explicitly
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
     * Set the {@link ToolCategoryRegistry}.
     *
     * @param toolCategoryRegistry the category registry
     *
     * @throws IllegalStateException if the registry has already been set
     */
    public void setToolCategoryRegistry(@NotNull ToolCategoryRegistry toolCategoryRegistry) {
        if (this.toolCategoryRegistry != null) {
            throw new IllegalStateException("toolCategoryRegistry has already been set");
        }

        this.toolCategoryRegistry = toolCategoryRegistry;
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
     * Set the implementation of {@link PlatformReconstructor}.
     *
     * @param platformReconstructor the instance to set
     *
     * @throws IllegalStateException if the PlatformReconstructor has already been set
     */
    public void setPlatformReconstructor(@NotNull PlatformReconstructor platformReconstructor) {
        if (this.platformReconstructor != null) {
            throw new IllegalStateException("platformReconstructor has already been set");
        }

        this.platformReconstructor = platformReconstructor;
    }

    /**
     * Get the {@link PlatformReconstructor} instance.
     *
     * @return the PlatformReconstructor instance
     */
    @NotNull
    public PlatformReconstructor getPlatformReconstructor() {
        if (toolCategoryRegistry == null) {
            throw new IllegalStateException("platformReconstructor has not been set.");
        }

        return platformReconstructor;
    }

    /**
     * Get the {@link VeinMiner} instance.
     *
     * @return the vein miner instance
     */
    @NotNull
    public static VeinMiner getInstance() {
        return (instance != null) ? instance : (instance = new VeinMiner());
    }

}
