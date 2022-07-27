package wtf.choco.veinminer;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessageProtocol;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A class holding VeinMiner's core common functionality.
 */
public interface VeinMiner {

    /**
     * A pattern to match a Minecraft block state. e.g. {@code minecraft:chest[waterlogged=false]}
     * <ul>
     *   <li><strong>Group 1</strong>: The block's type
     *   <li><strong>Group 2</strong>: The block's states (without the [] brackets)
     * </ul>
     */
    //                                                                    namespace    :key (optional)   [key=value,state=true]
    public static final Pattern PATTERN_BLOCK_STATE = Pattern.compile("^([a-z0-9._-]+(?::[a-z0-9/._-]+)*)(?:\\[(.+=.+)*\\])*$");

    /**
     * The {@link NamespacedKey} of the VeinMiner messaging channel.
     *
     * @see PluginMessageProtocol#getChannel() VeinMiner.PROTOCOL.getChannel()
     */
    public static final NamespacedKey PROTOCOL_CHANNEL = NamespacedKey.veinminer("veinminer");

    /**
     * The version of the VeinMiner protocol.
     *
     * @see PluginMessageProtocol#getVersion() VeinMiner.PROTOCOL.getVersion()
     */
    public static final int PROTOCOL_VERSION = 1;

    /**
     * VeinMiner's messaging protocol.
     *
     * @see PluginMessageProtocol
     */
    public static final PluginMessageProtocol PROTOCOL = new PluginMessageProtocol(PROTOCOL_CHANNEL, PROTOCOL_VERSION,
            serverboundRegistry -> serverboundRegistry
                .registerMessage(PluginMessageServerboundHandshake.class, PluginMessageServerboundHandshake::new)
                .registerMessage(PluginMessageServerboundToggleVeinMiner.class, PluginMessageServerboundToggleVeinMiner::new)
                .registerMessage(PluginMessageServerboundRequestVeinMine.class, PluginMessageServerboundRequestVeinMine::new)
                .registerMessage(PluginMessageServerboundSelectPattern.class, PluginMessageServerboundSelectPattern::new),

            clientboundRegistry -> clientboundRegistry
                .registerMessage(PluginMessageClientboundHandshakeResponse.class, PluginMessageClientboundHandshakeResponse::new)
                .registerMessage(PluginMessageClientboundSyncRegisteredPatterns.class, PluginMessageClientboundSyncRegisteredPatterns::new)
                .registerMessage(PluginMessageClientboundSetConfig.class, PluginMessageClientboundSetConfig::new)
                .registerMessage(PluginMessageClientboundVeinMineResults.class, PluginMessageClientboundVeinMineResults::new)
                .registerMessage(PluginMessageClientboundSetPattern.class, PluginMessageClientboundSetPattern::new)
    );

    /**
     * Get the version of VeinMiner (not to be confused with the protocol version).
     *
     * @return the version
     */
    @NotNull
    public String getVersion();

    /**
     * Check whether or not VeinMiner is running on the server.
     *
     * @return true if running on the server, false if running on the client
     *
     * @see #isClient()
     */
    public boolean isServer();

    /**
     * Check whether or not VeinMiner is running on the client.
     *
     * @return true if running on the client, false if running on the server
     *
     * @see #isServer()
     */
    public default boolean isClient() {
        return !isServer();
    }

}
