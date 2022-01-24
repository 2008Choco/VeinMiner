package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;

/**
 * A serverbound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>varint</strong>: protocol version
 * </ol>
 * Sent when the client joins the server.
 */
public final class PluginMessageServerboundHandshake implements PluginMessage<ServerboundPluginMessageListener> {

    private final int protocolVersion;

    public PluginMessageServerboundHandshake(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public PluginMessageServerboundHandshake(PluginMessageByteBuffer buffer) {
        this.protocolVersion = buffer.readVarInt();
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeVarInt(protocolVersion);
    }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleServerboundHandshake(this);
    }

}
