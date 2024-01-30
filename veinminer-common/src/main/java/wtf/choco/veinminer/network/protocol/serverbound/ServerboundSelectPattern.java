package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;

/**
 * A server bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>NamespacedKey</strong>: The key of the pattern the client wishes to select
 * </ol>
 * Sent when the client wants to change vein mining patterns.
 */
public final class ServerboundSelectPattern implements Message<VeinMinerServerboundMessageListener> {

    private final NamespacedKey patternKey;

    /**
     * Construct a new {@link ServerboundSelectPattern}.
     *
     * @param patternKey the {@link NamespacedKey} of the pattern to select
     */
    public ServerboundSelectPattern(@NotNull NamespacedKey patternKey) {
        this.patternKey = patternKey;
    }

    /**
     * Construct a new {@link ServerboundSelectPattern} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ServerboundSelectPattern(@NotNull MessageByteBuffer buffer) {
        this.patternKey = buffer.read(NamespacedKey.class);
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
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.write(patternKey);
    }

    @Override
    public void handle(@NotNull VeinMinerServerboundMessageListener listener) {
        listener.handleSelectPattern(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Select Pattern")
            .description("""
                    Sent by the client when it wants to change vein mining patterns as a result of a key press. The server is expected to respond with a Set Pattern message to confirm that the requested pattern is to be set on the client, however the server is not guaranteed to respond in the event that the request was unsuccessful.
                    """)
            .field(MessageField.TYPE_NAMESPACED_KEY, "Pattern Key", "The key of the pattern to request to be set");
    }

}
