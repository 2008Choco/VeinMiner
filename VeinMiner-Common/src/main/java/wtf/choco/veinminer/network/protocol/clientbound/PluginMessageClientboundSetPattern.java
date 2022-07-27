package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundSelectPattern;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A client bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>NamespacedKey</strong>: The key of the pattern to set on the client
 * </ol>
 * Sent in response to the client sending the {@link PluginMessageServerboundSelectPattern} message,
 * or if the server chooses to change the client's pattern.
 */
public final class PluginMessageClientboundSetPattern implements PluginMessage<ClientboundPluginMessageListener> {

    private final NamespacedKey patternKey;

    /**
     * Construct a new {@link PluginMessageClientboundSetPattern}.
     *
     * @param patternKey the pattern key to set
     */
    public PluginMessageClientboundSetPattern(@NotNull NamespacedKey patternKey) {
        this.patternKey = patternKey;
    }

    @Internal
    public PluginMessageClientboundSetPattern(@NotNull PluginMessageByteBuffer buffer) {
        this.patternKey = buffer.readNamespacedKey();
    }

    /**
     * Get the {@link NamespacedKey} of the pattern to set on the client.
     *
     * @return the pattern key
     */
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
