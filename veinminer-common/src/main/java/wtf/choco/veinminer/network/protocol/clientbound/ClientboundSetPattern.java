package wtf.choco.veinminer.network.protocol.clientbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundSelectPattern;

/**
 * A client bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>NamespacedKey</strong>: The key of the pattern to set on the client
 * </ol>
 * Sent in response to the client sending the {@link ServerboundSelectPattern} message,
 * or if the server chooses to change the client's pattern.
 */
public final class ClientboundSetPattern implements Message<VeinMinerClientboundMessageListener> {

    private final NamespacedKey patternKey;

    /**
     * Construct a new {@link ClientboundSetPattern}.
     *
     * @param patternKey the pattern key to set
     */
    public ClientboundSetPattern(@NotNull NamespacedKey patternKey) {
        this.patternKey = patternKey;
    }

    /**
     * Construct a new {@link ClientboundSetPattern} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ClientboundSetPattern(@NotNull MessageByteBuffer buffer) {
        this.patternKey = buffer.read(NamespacedKey.class);
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
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.write(patternKey);
    }

    @Override
    public void handle(@NotNull VeinMinerClientboundMessageListener listener) {
        listener.handleSetPattern(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Set Pattern")
            .description("""
                    Sets the selected pattern on the client. Sent in response to a server-bound Select Pattern message from the client, or when set by the server manually. If the client does not recognize this pattern key, the client should fall back to the pattern at index 0 of the patterns that were sent by the Sync Registered Patterns message.
                    """)
            .field(MessageField.TYPE_NAMESPACED_KEY, "Pattern Key", "The pattern key to set on the client");
    }

}
