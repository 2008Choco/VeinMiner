package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;

/**
 * A client bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>boolean</strong>: whether or not the server allows activation of vein miner on the client
 * </ol>
 * Sent in response to the client sending the {@link PluginMessageServerboundHandshake} message.
 */
public final class PluginMessageClientboundHandshakeResponse implements PluginMessage<ClientboundPluginMessageListener> {

    private final boolean enabled;

    /**
     * Construct a new {@link PluginMessageClientboundHandshakeResponse}.
     *
     * @param enabled whether or not vein miner is allowed for the client
     */
    public PluginMessageClientboundHandshakeResponse(boolean enabled) {
        this.enabled = enabled;
    }

    @Internal
    public PluginMessageClientboundHandshakeResponse(@NotNull PluginMessageByteBuffer buffer) {
        this.enabled = buffer.readBoolean();
    }

    /**
     * Check whether or not vein miner is allowed to use a client-activated key bind.
     * <p>
     * Clients are expected to respect this value. If this value is {@code true}, a client
     * should never send a {@link PluginMessageServerboundToggleVeinMiner} message. If it
     * does continue to send messages, they will be ignored.
     *
     * @return true if enabled, false if disabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeBoolean(enabled);
    }

    @Override
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
        listener.handleHandshakeResponse(this);
    }

}
