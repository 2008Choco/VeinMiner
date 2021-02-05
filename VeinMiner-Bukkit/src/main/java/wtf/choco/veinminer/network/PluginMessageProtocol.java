package wtf.choco.veinminer.network;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a protocol definition by which plugin messages ("custom packets") may be registered
 * and parsed in a more convenient and object-oriented way. Plugins can define their own protocols
 * and register custom {@link PluginMessage} implementations.
 *
 * <pre>
 *  private final PluginMessageProtocol{@literal <}VeinMiner{@literal >} pluginMessageProtocol = new PluginMessageProtocol{@literal <>}(veinminerInstance, "veinminer:bukkit", 1,
 *      serverRegistry -> serverRegistry
 *          .registerMessage(PluginMessageInHandshake.class, PluginMessageInHandshake::new) // 0x00
 *          .registerMessage(PluginMessageInToggleVeinMiner.class, PluginMessageInToggleVeinMiner::new), // 0x01
 *
 *      clientRegistry -> clientRegistry
 *          .registerMessage(PluginMessageOutExampleMessage.class, PluginMessageOutExampleMessage::new) // 0x00
 *  );
 * </pre>
 *
 * The above may be used to send clientbound/receive serverbound {@link PluginMessage PluginMessages}
 * to and from third party software listening for Minecraft's custom payload packet.
 *
 * @param <T> the plugin to which the protocol belongs
 *
 * @see PluginMessage
 * @see PluginMessageByteBuffer
 */
public class PluginMessageProtocol<@NotNull T extends Plugin> {

    private final T plugin;
    private final String channel;
    private final int version;

    private final Map<@NotNull MessageDirection, @NotNull PluginMessageRegistry> registries = new EnumMap<>(MessageDirection.class);

    /**
     * Construct a new {@link PluginMessageProtocol}.
     *
     * @param plugin the plugin instance
     * @param channel the channel on which this protocol is registered
     * @param version the protocol version
     * @param serverboundMessageSupplier the supplier to which serverbound messages should be registered
     * @param clientboundMessageSupplier the supplier to which clientbound messages should be registered
     */
    public PluginMessageProtocol(@NotNull T plugin, @NotNull String channel, int version, @NotNull Consumer<@NotNull PluginMessageRegistry> serverboundMessageSupplier, @NotNull Consumer<@NotNull PluginMessageRegistry> clientboundMessageSupplier) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
        Preconditions.checkArgument(channel != null, "channel must not be null");
        Preconditions.checkArgument(serverboundMessageSupplier != null, "serverboundMessageSupplier must not be null");
        Preconditions.checkArgument(clientboundMessageSupplier != null, "clientboundMessageSupplier must not be null");

        this.plugin = plugin;
        this.channel = channel;
        this.version = version;

        this.constructAndRegisterRegistry(MessageDirection.SERVERBOUND, serverboundMessageSupplier);
        this.constructAndRegisterRegistry(MessageDirection.CLIENTBOUND, clientboundMessageSupplier);
    }

    /**
     * Get the {@link Plugin} instance to which this protocol belongs.
     *
     * @return the plugin
     */
    @NotNull
    public T getPlugin() {
        return plugin;
    }

    /**
     * Get the channel on which this protocol is listening.
     *
     * @return the channel
     */
    @NotNull
    public String getChannel() {
        return channel;
    }

    /**
     * Get the protocol version.
     *
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Send the provided {@link PluginMessage} to the given {@link Player}.
     *
     * @param player the player to which the message should be sent
     * @param message the message to send
     */
    public void sendMessage(@NotNull Player player, @NotNull PluginMessage<T> message) {
        Preconditions.checkArgument(player != null, "player must not be null");
        Preconditions.checkArgument(message != null, "message must not be null");

        int messageId = getPluginMessageId(MessageDirection.CLIENTBOUND, message);
        if (messageId < 0) {
            throw new IllegalStateException("Invalid plugin message, " + message.getClass().getName() + ". Is it registered?");
        }

        PluginMessageByteBuffer buffer = new PluginMessageByteBuffer();
        buffer.writeVarInt(messageId);
        message.write(buffer);

        player.sendPluginMessage(plugin, channel, buffer.asByteArray());
    }

    /**
     * Get the integer id of the given {@link PluginMessage} and {@link MessageDirection}.
     *
     * @param direction the message direction
     * @param message the message whose id to get
     *
     * @return the message id
     */
    public int getPluginMessageId(@NotNull MessageDirection direction, @NotNull PluginMessage<T> message) {
        return getPluginMessageId(direction, message.getClass());
    }

    /**
     * Get the integer id of the given {@link PluginMessage} and {@link MessageDirection}.
     *
     * @param direction the message direction
     * @param messageClass the class of the message whose id to get
     *
     * @return the message id
     */
    public int getPluginMessageId(@NotNull MessageDirection direction, @NotNull Class<?> messageClass) {
        Preconditions.checkArgument(direction != null, "direction must not be null");
        Preconditions.checkArgument(messageClass != null, "messageClass must not be null");

        return registries.get(direction).messageIds.get(messageClass);
    }

    /**
     * Create a new {@link PluginMessage} instance from its registered id.
     *
     * @param direction the message direction
     * @param messageId the id of the message to create
     *
     * @return the created plugin message
     */
    public PluginMessage<T> createPluginMessage(@NotNull MessageDirection direction, int messageId) {
        Preconditions.checkArgument(direction != null, "direction must not be null");

        return registries.get(direction).createPluginMessage(messageId);
    }

    /**
     * Check whether or not the given message id is registered and valid under the given
     * message direction for this protocol.
     *
     * @param direction the message direction
     * @param messageId the id of the message to check
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidMessageId(@NotNull MessageDirection direction, int messageId) {
        Preconditions.checkArgument(direction != null, "direction must not be null");

        return messageId >= 0 && messageId < registries.get(direction).getRegisteredMessageAmount();
    }

    private void constructAndRegisterRegistry(@NotNull MessageDirection direction, @Nullable Consumer<@NotNull PluginMessageRegistry> messageSupplier) {
        Preconditions.checkArgument(direction != null, "direction must not be null");

        if (messageSupplier == null) {
            return;
        }

        PluginMessageRegistry registry = new PluginMessageRegistry();
        this.registries.put(direction, registry);
        messageSupplier.accept(registry);

        // Register the message handlers to Bukkit
        Messenger messenger = Bukkit.getMessenger();
        if (direction.isServerbound()) {
            messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, data) -> {
                PluginMessageByteBuffer buffer = new PluginMessageByteBuffer(ByteBuffer.wrap(data));

                try {
                    int messageId = buffer.readVarInt();
                    if (!isValidMessageId(MessageDirection.SERVERBOUND, messageId)) {
                        player.kickPlayer("Received invalid packet with id " + messageId + " (" + channelName + "). Contact an administrator.");
                        return;
                    }

                    PluginMessage<T> message = createPluginMessage(MessageDirection.SERVERBOUND, messageId);
                    if (message == null) {
                        player.kickPlayer("Received unrecognized packet with id " + messageId + " (" + channelName + "). Contact an administrator.");
                        return;
                    }

                    message.read(buffer);
                    message.handle(plugin, player);
                } catch (IllegalStateException e) {
                    player.kickPlayer("Malformatted or invalid packet (" + channelName + "). Contact an administrator.");
                }
            });
        }
        else if (direction.isClientbound()) {
            messenger.registerOutgoingPluginChannel(plugin, channel);
        }
    }

    /**
     * Represents an internal registry mapping plugin messages to ids and constructors.
     */
    public final class PluginMessageRegistry {

        private final Map<@NotNull Class<? extends PluginMessage<T>>, Integer> messageIds = new IdentityHashMap<>();
        private final List<@NotNull Supplier<@NotNull ? extends PluginMessage<T>>> messageConstructors = new ArrayList<>();

        private PluginMessageRegistry() { }

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
        public <M extends PluginMessage<T>> PluginMessageRegistry registerMessage(@NotNull Class<M> messageClass, @NotNull Supplier<@NotNull M> messageConstructor) {
            Preconditions.checkArgument(messageClass != null, "messageClass must not be null");
            Preconditions.checkArgument(messageConstructor != null, "messageConstructor must not be null");

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

        @NotNull
        private PluginMessage<T> createPluginMessage(int messageId) {
            Preconditions.checkArgument(messageId >= 0 && messageId < messageConstructors.size(), "Unregistered plugin message id %d", messageId);

            Supplier<? extends PluginMessage<T>> messageConstructor = messageConstructors.get(messageId);
            return messageConstructor.get();
        }

    }

}
