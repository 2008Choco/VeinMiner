package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;

/**
 * A client bound {@link PluginMessage} with no data.
 * <p>
 * Sent in response to the client sending the {@link PluginMessageServerboundHandshake} message.
 */
public final class PluginMessageClientboundHandshakeResponse implements PluginMessage<ClientboundPluginMessageListener> {

    /*
     * At the moment, this message serves no purpose other than to inform the client that
     * the server has acknowledged its presence. In the future, this message may be used to return
     * to the client crucial information.
     */

    /**
     * Construct a new {@link PluginMessageClientboundHandshakeResponse}.
     */
    public PluginMessageClientboundHandshakeResponse() { }

    /**
     * Construct a new {@link PluginMessageClientboundHandshakeResponse} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public PluginMessageClientboundHandshakeResponse(@NotNull PluginMessageByteBuffer buffer) { }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) { }

    @Override
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
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
