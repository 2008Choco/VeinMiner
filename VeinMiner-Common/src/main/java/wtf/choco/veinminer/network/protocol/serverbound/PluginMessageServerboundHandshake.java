package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
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

    /**
     * Construct a new {@link PluginMessageServerboundHandshake} with input.
     *
     * @param buffer the input buffer
     */
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

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Handshake (serverbound)")
            .description("""
                    Sent by the client when logging in to inform the server that the client has the VeinMiner client-sided mod installed. The server is expected to respond promptly with a Handshake Response. Upon receiving this message, the server will automatically set the player's activation mode to CLIENT.
                    """)
            .field(MessageField.TYPE_VARINT, "Protocol Version", "The client's protocol version");
    }

}
