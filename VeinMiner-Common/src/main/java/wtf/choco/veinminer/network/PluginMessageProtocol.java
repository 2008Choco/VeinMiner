package wtf.choco.veinminer.network;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * Represents a protocol definition by which plugin messages ("custom packets") may be registered
 * and parsed in a more convenient and object-oriented way. Plugins can define their own protocols
 * and register custom {@link PluginMessage} implementations.
 * <pre>
 * public static final PluginMessageProtocol PROTOCOL = new PluginMessageProtocol(new NamespacedKey("namespace", "key"), 1,
 *     serverRegistry -> serverRegistry
 *         .registerMessage(PluginMessageServerboundExampleOne.class, PluginMessageServerboundExampleOne::new) // 0x00
 *         .registerMessage(PluginMessageServerboundExampleTwo.class, PluginMessageServerboundExampleTwo::new), // 0x01
 *
 *     clientRegistry -> clientRegistry
 *         .registerMessage(PluginMessageClientboundExampleOne.class, PluginMessageClientboundExampleOne::new) // 0x00
 * );
 *
 * { // Somewhere in initialization, the channels have to be registered with a ChannelRegistrar
 *     PROTOCOL.registerChannels(new MyChannelRegisrarImplementation());
 *
 *     // Now things are ready to go and send messages
 *     PROTOCOL.sendServerMessage(messageReceiver, new PluginMessageServerboundExampleOne("some parameter", "whatever data you want in here", 10));
 * }
 * </pre>
 * The above may be used to send or receive client or server bound PluginMessages to and from
 * third party software listening for Minecraft's custom payload packet.
 *
 * @see ChannelRegistrar
 * @see PluginMessage
 * @see PluginMessageByteBuffer
 */
public final class PluginMessageProtocol {

    private final NamespacedKey channel;
    private final int version;

    private final Map<@NotNull MessageDirection, @NotNull PluginMessageRegistry<?>> registries = new EnumMap<>(MessageDirection.class);

    private final Consumer<PluginMessageRegistry<ServerboundPluginMessageListener>> serverboundMessageSupplier;
    private final Consumer<PluginMessageRegistry<ClientboundPluginMessageListener>> clientboundMessageSupplier;

    /**
     * Construct a new {@link PluginMessageProtocol}.
     *
     * @param channel the channel on which this protocol is registered
     * @param version the protocol version
     * @param serverboundMessageSupplier the supplier to which server-bound messages should be registered
     * @param clientboundMessageSupplier the supplier to which client-bound messages should be registered
     */
    public PluginMessageProtocol(@NotNull NamespacedKey channel, int version, @NotNull Consumer<PluginMessageRegistry<ServerboundPluginMessageListener>> serverboundMessageSupplier, @NotNull Consumer<PluginMessageRegistry<ClientboundPluginMessageListener>> clientboundMessageSupplier) {
        this.channel = channel;
        this.version = version;

        this.serverboundMessageSupplier = serverboundMessageSupplier;
        this.clientboundMessageSupplier = clientboundMessageSupplier;
    }

    /**
     * Get the channel on which this protocol is listening.
     *
     * @return the channel
     */
    @NotNull
    public NamespacedKey getChannel() {
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
     * Send a client-bound {@link PluginMessage} to the given {@link MessageReceiver}.
     *
     * @param receiver the receiver to which the message should be send
     * @param message the message to send
     */
    public void sendMessageToClient(@NotNull MessageReceiver receiver, @NotNull PluginMessage<?> message) {
        this.sendMessageTo(MessageDirection.CLIENTBOUND, receiver, message);
    }

    /**
     * Send a server-bound {@link PluginMessage} to the given {@link MessageReceiver}.
     *
     * @param receiver the receiver to which the message should be send
     * @param message the message to send
     */
    public void sendMessageToServer(@NotNull MessageReceiver receiver, @NotNull PluginMessage<?> message) {
        this.sendMessageTo(MessageDirection.SERVERBOUND, receiver, message);
    }

    private void sendMessageTo(@NotNull MessageDirection direction, @NotNull MessageReceiver receiver, @NotNull PluginMessage<?> message) {
        int messageId = registries.get(direction).getPluginMessageId(message.getClass());
        if (messageId < 0) {
            throw new IllegalStateException("Invalid plugin message, " + message.getClass().getName() + ". Is it registered?");
        }

        PluginMessageByteBuffer buffer = new PluginMessageByteBuffer();
        buffer.writeVarInt(messageId);
        message.write(buffer);

        receiver.sendMessage(channel, buffer.asByteArray());
    }

    /**
     * Register messaging channels with the given {@link ChannelRegistrar}.
     *
     * @param registrar the registrar
     */
    public void registerChannels(@NotNull ChannelRegistrar registrar) {
        if (serverboundMessageSupplier != null) {
            PluginMessageRegistry<ServerboundPluginMessageListener> registry = new PluginMessageRegistry<>();
            this.registries.put(MessageDirection.SERVERBOUND, registry);
            this.serverboundMessageSupplier.accept(registry);

            registrar.registerServerboundMessageHandler(channel, registry);
        }

        if (clientboundMessageSupplier != null) {
            PluginMessageRegistry<ClientboundPluginMessageListener> registry = new PluginMessageRegistry<>();
            this.registries.put(MessageDirection.CLIENTBOUND, registry);
            this.clientboundMessageSupplier.accept(registry);

            registrar.registerClientboundMessageHandler(channel, registry);
        }
    }

}
