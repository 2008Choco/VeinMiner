package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A client bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>VarInt</strong>: The amount of registered keys
 *   <li><strong>Array of NamespacedKey</strong>: The registered keys
 * </ol>
 * Sent when the server wants to synchronize the keys of all vein mining patterns registered on
 * the server. The server is expected to send this message after the client has successfully
 * shaken hands with the server.
 */
public final class PluginMessageClientboundSyncRegisteredPatterns implements PluginMessage<ClientboundPluginMessageListener> {

    private final List<NamespacedKey> keys;

    /**
     * Construct a new {@link PluginMessageClientboundSyncRegisteredPatterns}.
     *
     * @param keys the registered keys
     */
    public PluginMessageClientboundSyncRegisteredPatterns(@NotNull List<NamespacedKey> keys) {
        this.keys = keys;
    }

    @Internal
    public PluginMessageClientboundSyncRegisteredPatterns(@NotNull PluginMessageByteBuffer buffer) {
        int size = buffer.readVarInt();
        this.keys = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            NamespacedKey key = buffer.readNamespacedKey();
            this.keys.add(key);
        }
    }

    /**
     * Get a {@link List} of all registered pattern keys.
     *
     * @return all pattern keys
     */
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

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Sync Registered Patterns")
            .description("""
                    Sent by the server after the client has successfully shaken hands and has been sent the handshake response. Synchronizes the server's registered pattern keys with the client so that it may switch between patterns using a key bind.
                    """)
            .field(MessageField.TYPE_VARINT, "Size", "The amount of patterns being sent to the client")
            .field(MessageField.TYPE_ARRAY_OF.apply(MessageField.TYPE_NAMESPACED_KEY), "Pattern Keys", "An array containing the namespaced keys of all vein mining patterns registered on the server");
    }

}
