package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;

/**
 * A serverbound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>boolean</strong>: whether or not vein miner is active
 * </ol>
 * Sent when the client presses or releases the vein miner activating keybind.
 */
public final class PluginMessageServerboundToggleVeinMiner implements PluginMessage<ServerboundPluginMessageListener> {

    private final boolean activated;

    public PluginMessageServerboundToggleVeinMiner(boolean activated) {
        this.activated = activated;
    }

    public PluginMessageServerboundToggleVeinMiner(@NotNull PluginMessageByteBuffer buffer) {
        this.activated = buffer.readBoolean();
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeBoolean(activated);
    }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleServerboundToggleVeinMiner(this);
    }

}
