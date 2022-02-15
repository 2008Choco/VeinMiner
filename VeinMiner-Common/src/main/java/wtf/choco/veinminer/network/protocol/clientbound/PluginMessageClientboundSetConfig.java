package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;

/**
 * A client bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>byte</strong>: A bitmask containing 3 booleans determining the client configuration
 * </ol>
 * Sent after the server has responded to the client's handshake, or when the server's configuration
 * has been reloaded.
 */
public final class PluginMessageClientboundSetConfig implements PluginMessage<ClientboundPluginMessageListener> {

    private final ClientConfig config;

    public PluginMessageClientboundSetConfig(@NotNull ClientConfig config) {
        this.config = config;
    }

    @Internal
    public PluginMessageClientboundSetConfig(@NotNull PluginMessageByteBuffer buffer) {
        this.config = ClientConfig.builder().applyBitmask(buffer.readByte()).build();
    }

    @NotNull
    public ClientConfig getConfig() {
        return config;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeByte(config.getBooleanValuesAsBitmask());
    }

    @Override
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
        listener.handleSetConfig(this);
    }

}
