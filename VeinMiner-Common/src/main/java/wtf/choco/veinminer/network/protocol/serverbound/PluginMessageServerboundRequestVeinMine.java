package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;

public final class PluginMessageServerboundRequestVeinMine implements PluginMessage<ServerboundPluginMessageListener> {

    public PluginMessageServerboundRequestVeinMine() { }

    public PluginMessageServerboundRequestVeinMine(@SuppressWarnings("unused") @NotNull PluginMessageByteBuffer buffer) { }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) { }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleRequestVeinMine(this);
    }

}
