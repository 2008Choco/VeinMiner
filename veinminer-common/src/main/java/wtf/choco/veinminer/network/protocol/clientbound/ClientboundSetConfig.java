package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;

/**
 * A client bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>byte</strong>: A bitmask containing 3 booleans determining the client configuration
 * </ol>
 * Sent after the server has responded to the client's handshake, or when the server's configuration
 * has been reloaded.
 */
public final class ClientboundSetConfig implements Message<VeinMinerClientboundMessageListener> {

    private final ClientConfig config;

    /**
     * Construct a new {@link ClientboundSetConfig}.
     *
     * @param config the config to send
     */
    public ClientboundSetConfig(@NotNull ClientConfig config) {
        this.config = config;
    }

    /**
     * Construct a new {@link ClientboundSetConfig} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ClientboundSetConfig(@NotNull MessageByteBuffer buffer) {
        this.config = ClientConfig.builder().applyBitmask(buffer.readByte()).build();
    }

    /**
     * Get the {@link ClientConfig} that was sent.
     *
     * @return the client config
     */
    @NotNull
    public ClientConfig getConfig() {
        return config;
    }

    @Override
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.writeByte(config.getBooleanValuesAsBitmask());
    }

    @Override
    public void handle(@NotNull VeinMinerClientboundMessageListener listener) {
        listener.handleSetConfig(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Set Config")
            .description("""
                    Sent by the server after the client's handshake, or when the server reloads its configuration, to set the client's capabilities. The client is expected to respect these values to avoid network overhead, but the server performs additional checks and will not respond to incoming messages if a specific feature is disabled by the server.
                    """)
            .field(MessageField.bitmask(
                    "If the activation keybind is allowed",
                    "If the pattern switching keybinds are allowed",
                    "If the client is allowed to render a wireframe around vein mine results"
            ), "Config Bitmask", "A bitmask of configured values");
    }

}
