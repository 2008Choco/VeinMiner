package wtf.choco.veinminer;

import java.util.regex.Pattern;

import wtf.choco.network.MessageProtocol;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.network.VeinMinerProtocolConfiguration;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundToggleVeinMiner;

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
    //                                                                    namespace    :key (optional)   [key=value,state=true]
    public static final Pattern PATTERN_BLOCK_STATE = Pattern.compile("^([a-z0-9._-]+(?::[a-z0-9/._-]+)*)(?:\\[(.+=.+)*\\])*$");

    /**
     * The {@link NamespacedKey} of the VeinMiner messaging channel.
     *
     * @see MessageProtocol#getChannel() VeinMiner.PROTOCOL.getChannel()
     */
    public static final NamespacedKey PROTOCOL_CHANNEL = NamespacedKey.of("veinminer", "veinminer");

    /**
     * The version of the VeinMiner protocol.
     *
     * @see MessageProtocol#getVersion() VeinMiner.PROTOCOL.getVersion()
     */
    public static final int PROTOCOL_VERSION = 1;

    /**
     * VeinMiner's messaging protocol.
     *
     * @see MessageProtocol
     */
    public static final MessageProtocol<VeinMinerServerboundMessageListener, VeinMinerClientboundMessageListener> PROTOCOL = new MessageProtocol<VeinMinerServerboundMessageListener, VeinMinerClientboundMessageListener>(
            PROTOCOL_CHANNEL, PROTOCOL_VERSION,
            serverboundRegistry -> serverboundRegistry
                .registerMessage(ServerboundHandshake.class, ServerboundHandshake::new)
                .registerMessage(ServerboundToggleVeinMiner.class, ServerboundToggleVeinMiner::new)
                .registerMessage(ServerboundRequestVeinMine.class, ServerboundRequestVeinMine::new)
                .registerMessage(ServerboundSelectPattern.class, ServerboundSelectPattern::new),
            clientboundRegistry -> clientboundRegistry
                .registerMessage(ClientboundHandshakeResponse.class, ClientboundHandshakeResponse::new)
                .registerMessage(ClientboundSyncRegisteredPatterns.class, ClientboundSyncRegisteredPatterns::new)
                .registerMessage(ClientboundSetConfig.class, ClientboundSetConfig::new)
                .registerMessage(ClientboundVeinMineResults.class, ClientboundVeinMineResults::new)
                .registerMessage(ClientboundSetPattern.class, ClientboundSetPattern::new)
    ).configure(new VeinMinerProtocolConfiguration());

    private VeinMiner() { }

}
