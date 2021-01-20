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
public class PluginMessageProtocol<T extends Plugin> {

    private final T plugin;
    private final String channel;
    private final int version;

    private final Map<MessageDirection, PluginMessageRegistry> registries = new EnumMap<>(MessageDirection.class);

    /**
     * Construct a new {@link PluginMessageProtocol}.
     *
     * @param plugin the plugin instance
     * @param channel the channel on which this protocol is registered
     * @param version the protocol version
     * @param serverboundMessageSupplier the supplier to which serverbound messages should be registered
     * @param clientboundMessageSupplier the supplier to which clientbound messages should be registered
     */
    public PluginMessageProtocol(T plugin, String channel, int version, Consumer<PluginMessageRegistry> serverboundMessageSupplier, Consumer<PluginMessageRegistry> clientboundMessageSupplier) {
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
    public T getPlugin() {
        return plugin;
    }

    /**
     * Get the channel on which this protocol is listening.
     *
     * @return the channel
     */
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
    public void sendMessage(Player player, PluginMessage<T> message) {
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
    public int getPluginMessageId(MessageDirection direction, PluginMessage<T> message) {
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
    public int getPluginMessageId(MessageDirection direction, Class<?> messageClass) {
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
    public PluginMessage<T> createPluginMessage(MessageDirection direction, int messageId) {
        return registries.get(direction).createPluginMessage(messageId);
    }

    private void constructAndRegisterRegistry(MessageDirection direction, Consumer<PluginMessageRegistry> messageSupplier) {
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

                int messageId = buffer.readVarInt();
                PluginMessage<T> message = createPluginMessage(MessageDirection.SERVERBOUND, messageId);
                if (message == null) {
                    throw new IllegalStateException("Received invalid plugin message with id " + messageId + ". Don't know what to do here...");
                }

                message.read(buffer);
                message.handle(plugin, player);
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

        private final Map<Class<? extends PluginMessage<T>>, Integer> messageIds = new IdentityHashMap<>();
        private final List<Supplier<? extends PluginMessage<T>>> messageConstructors = new ArrayList<>();

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
        public <M extends PluginMessage<T>> PluginMessageRegistry registerMessage(Class<M> messageClass, Supplier<M> messageConstructor) {
            int messageId = messageIds.size();

            Integer existingMessageId = messageIds.put(messageClass, messageId);
            if (existingMessageId != null) {
                throw new IllegalStateException("Attempted to register plugin message " + messageClass.getName() + " with id " + existingMessageId.intValue() + " but is already registered.");
            }

            this.messageConstructors.add(messageConstructor);
            return this;
        }

        private PluginMessage<T> createPluginMessage(int messageId) {
            Preconditions.checkArgument(messageId >= 0 && messageId < messageConstructors.size(), "Unregistered plugin message id %d", messageId);

            Supplier<? extends PluginMessage<T>> messageConstructor = messageConstructors.get(messageId);
            return messageConstructor.get();
        }

    }

}
