package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

public final class PluginMessageClientboundSyncRegisteredPatterns implements PluginMessage<ClientboundPluginMessageListener> {

    private final List<NamespacedKey> keys;

    public PluginMessageClientboundSyncRegisteredPatterns(@NotNull List<NamespacedKey> keys) {
        this.keys = keys;
    }

    public PluginMessageClientboundSyncRegisteredPatterns(@NotNull PluginMessageByteBuffer buffer) {
        int size = buffer.readVarInt();
        this.keys = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            NamespacedKey key = buffer.readNamespacedKey();
            this.keys.add(key);
        }
    }

    public List<NamespacedKey> getKeys() {
        return keys;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeVarInt(keys.size());
        this.keys.forEach(buffer::writeNamespacedKey);
    }

    @Override
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
        listener.handleSyncRegisteredPatterns(this);
    }

}
