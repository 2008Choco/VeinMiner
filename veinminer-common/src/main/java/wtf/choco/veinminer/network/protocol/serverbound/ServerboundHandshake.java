package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;

/**
 * A server bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>VarInt</strong>: protocol version
 * </ol>
 * Sent when a client joins the server.
 */
public final class ServerboundHandshake implements Message<VeinMinerServerboundMessageListener> {

    private final int protocolVersion;

    /**
     * Construct a new {@link ServerboundHandshake}.
     *
     * @param protocolVersion the client's VeinMiner protocol version
     */
    public ServerboundHandshake(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Construct a new {@link ServerboundHandshake} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ServerboundHandshake(@NotNull MessageByteBuffer buffer) {
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
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.writeVarInt(protocolVersion);
    }

    @Override
    public void handle(@NotNull VeinMinerServerboundMessageListener listener) {
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
