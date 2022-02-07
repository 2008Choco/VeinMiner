package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.util.NamespacedKey;

public final class PluginMessageClientboundSetPattern implements PluginMessage<ClientboundPluginMessageListener> {

    private final NamespacedKey patternKey;

    public PluginMessageClientboundSetPattern(@NotNull NamespacedKey key) {
        this.patternKey = key;
    }

    public PluginMessageClientboundSetPattern(@NotNull VeinMiningPattern pattern) {
        this(pattern.getKey());
    }

    public PluginMessageClientboundSetPattern(@NotNull PluginMessageByteBuffer buffer) {
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
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
        listener.handleSetPattern(this);
    }

}
