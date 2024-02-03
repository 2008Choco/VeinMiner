package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;

/**
 * A client bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>VarInt</strong>: The amount of registered keys
 *   <li><strong>Array of NamespacedKey</strong>: The registered keys
 * </ol>
 * Sent when the server wants to synchronize the keys of all vein mining patterns registered on
 * the server. The server is expected to send this message after the client has successfully
 * shaken hands with the server.
 */
public final class ClientboundSyncRegisteredPatterns implements Message<VeinMinerClientboundMessageListener> {

    private final List<NamespacedKey> keys;

    /**
     * Construct a new {@link ClientboundSyncRegisteredPatterns}.
     *
     * @param keys the registered keys
     */
    public ClientboundSyncRegisteredPatterns(@NotNull List<NamespacedKey> keys) {
        this.keys = keys;
    }

    /**
     * Construct a new {@link ClientboundSyncRegisteredPatterns} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ClientboundSyncRegisteredPatterns(@NotNull MessageByteBuffer buffer) {
        int size = buffer.readVarInt();
        this.keys = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            NamespacedKey key = buffer.read(NamespacedKey.class);
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
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.writeVarInt(keys.size());
        this.keys.forEach(buffer::write);
    }

    @Override
    public void handle(@NotNull VeinMinerClientboundMessageListener listener) {
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
