package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A server bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>NamespacedKey</strong>: The key of the pattern the client wishes to select
 * </ol>
 * Sent when the client wants to change vein mining patterns.
 */
public final class PluginMessageServerboundSelectPattern implements PluginMessage<ServerboundPluginMessageListener> {

    private final NamespacedKey patternKey;

    /**
     * Construct a new {@link PluginMessageServerboundSelectPattern}.
     *
     * @param patternKey the {@link NamespacedKey} of the pattern to select
     */
    public PluginMessageServerboundSelectPattern(@NotNull NamespacedKey patternKey) {
        this.patternKey = patternKey;
    }

    @Internal
    public PluginMessageServerboundSelectPattern(@NotNull PluginMessageByteBuffer buffer) {
        this.patternKey = buffer.readNamespacedKey();
    }

    /**
     * Get the {@link NamespacedKey} of the pattern the client wants to change to.
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
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleSelectPattern(this);
    }

}
