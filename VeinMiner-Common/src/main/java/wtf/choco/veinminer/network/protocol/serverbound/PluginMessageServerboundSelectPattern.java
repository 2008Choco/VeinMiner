package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

public final class PluginMessageServerboundSelectPattern implements PluginMessage<ServerboundPluginMessageListener> {

    private final NamespacedKey patternKey;

    public PluginMessageServerboundSelectPattern(@NotNull NamespacedKey patternKey) {
        this.patternKey = patternKey;
    }

    public PluginMessageServerboundSelectPattern(@NotNull PluginMessageByteBuffer buffer) {
        this.patternKey = buffer.readNamespacedKey();
    }

    public NamespacedKey getPatternKey() {
        return patternKey;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeNamespacedKey(patternKey);
    }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleSelectPattern(this);
    }

}
