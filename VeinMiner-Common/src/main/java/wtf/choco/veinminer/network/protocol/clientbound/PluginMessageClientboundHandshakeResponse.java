package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;

public final class PluginMessageClientboundHandshakeResponse implements PluginMessage<ClientboundPluginMessageListener> {

    // TODO: Include configuration here

    private final boolean enabled;

    public PluginMessageClientboundHandshakeResponse(boolean enabled) {
        this.enabled = enabled;
    }

    public PluginMessageClientboundHandshakeResponse(PluginMessageByteBuffer buffer) {
        this.enabled = buffer.readBoolean();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeBoolean(enabled);
    }

    @Override
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
        listener.handleClientboundHandshakeResponse(this);
    }

}
