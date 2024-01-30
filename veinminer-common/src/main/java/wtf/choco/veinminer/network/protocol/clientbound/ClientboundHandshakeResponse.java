package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;

/**
 * A client bound {@link Message} with no data.
 * <p>
 * Sent in response to the client sending the {@link ServerboundHandshake} message.
 */
public final class ClientboundHandshakeResponse implements Message<VeinMinerClientboundMessageListener> {

    /*
     * At the moment, this message serves no purpose other than to inform the client that
     * the server has acknowledged its presence. In the future, this message may be used to return
     * to the client crucial information.
     */

    /**
     * Construct a new {@link ClientboundHandshakeResponse}.
     */
    public ClientboundHandshakeResponse() { }

    /**
     * Construct a new {@link ClientboundHandshakeResponse} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ClientboundHandshakeResponse(@NotNull MessageByteBuffer buffer) { }

    @Override
    public void write(@NotNull MessageByteBuffer buffer) { }

    @Override
    public void handle(@NotNull VeinMinerClientboundMessageListener listener) {
        listener.handleHandshakeResponse(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Handshake Response")
            .description("""
                    Sent in response to a client's Handshake. This message contains no additional data (yet) and acts primarily as a server acknowledgement of the client mod.
                    """);
    }

}
