package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;

/**
 * A server bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>VarInt</strong>: protocol version
 * </ol>
 * Sent when a client joins the server.
 */
public final class PluginMessageServerboundHandshake implements PluginMessage<ServerboundPluginMessageListener> {

    private final int protocolVersion;

    /**
     * Construct a new {@link PluginMessageServerboundHandshake}.
     *
     * @param protocolVersion the client's VeinMiner protocol version
     */
    public PluginMessageServerboundHandshake(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Internal
    public PluginMessageServerboundHandshake(@NotNull PluginMessageByteBuffer buffer) {
        this.protocolVersion = buffer.readVarInt();
    }

    /**
     * Get the client's VeinMiner protocol version.
     *
     * @return the protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeVarInt(protocolVersion);
    }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleHandshake(this);
    }

}
