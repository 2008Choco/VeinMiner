package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;

/**
 * A server bound {@link PluginMessage} with no data.
 * <p>
 * Sent by the client to request a vein mine at the player's target block position.
 */
public final class PluginMessageServerboundRequestVeinMine implements PluginMessage<ServerboundPluginMessageListener> {

    /**
     * Construct a new {@link PluginMessageServerboundRequestVeinMine}.
     */
    public PluginMessageServerboundRequestVeinMine() { }

    @Internal
    public PluginMessageServerboundRequestVeinMine(@SuppressWarnings("unused") @NotNull PluginMessageByteBuffer buffer) { }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) { }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleRequestVeinMine(this);
    }

}
