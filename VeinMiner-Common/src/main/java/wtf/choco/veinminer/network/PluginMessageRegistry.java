package wtf.choco.veinminer.network;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an internal registry mapping plugin messages to ids and constructors.
 *
 * @param <T> the type of plugin message listener for messages in this registry
 */
public final class PluginMessageRegistry<T extends PluginMessageListener> {

    private final Map<Class<? extends PluginMessage<T>>, Integer> messageIds = new IdentityHashMap<>();
    private final List<Function<PluginMessageByteBuffer, ? extends PluginMessage<T>>> messageConstructors = new ArrayList<>();

    /**
     * Construct a new {@link PluginMessageRegistry}.
     */
    PluginMessageRegistry() { }

    /**
     * Register a new {@link PluginMessage} to this protocol.
     *
     * @param <M> the message
     *
     * @param messageClass the message class
     * @param messageConstructor a supplier to construct the message
     *
     * @return this instance. Allows for chained message calls
     */
    @NotNull
    public <M extends PluginMessage<T>> PluginMessageRegistry<T> registerMessage(@NotNull Class<M> messageClass, @NotNull Function<PluginMessageByteBuffer, M> messageConstructor) {
        int messageId = messageIds.size();

        Integer existingMessageId = messageIds.put(messageClass, messageId);
        if (existingMessageId != null) {
            throw new IllegalStateException("Attempted to register plugin message " + messageClass.getName() + " with id " + existingMessageId.intValue() + " but is already registered.");
        }

        this.messageConstructors.add(messageConstructor);
        return this;
    }

    /**
     * Get the amount of messages registered to this message registry.
     *
     * @return the amount of messages
     */
    public int getRegisteredMessageAmount() {
        return messageConstructors.size();
    }

    /**
     * Get the id of the given plugin message class.
     *
     * @param message the class of the message whose id to get
     *
     * @return the id of the plugin message, or -1 if no message exists
     */
    public int getPluginMessageId(Class<?> message) {
        return messageIds.getOrDefault(message, -1);
    }

    /**
     * Create a {@link PluginMessage} with the given message id and the provided
     * {@link PluginMessageByteBuffer} data.
     *
     * @param messageId the id of the message to create
     * @param buffer the buffer containing message data
     *
     * @return the created message
     */
    @Nullable
    public PluginMessage<T> createPluginMessage(int messageId, @NotNull PluginMessageByteBuffer buffer) {
        Function<PluginMessageByteBuffer, ? extends PluginMessage<T>> messageConstructor = messageConstructors.get(messageId);
        return (messageConstructor != null) ? messageConstructor.apply(buffer) : null;
    }

}
