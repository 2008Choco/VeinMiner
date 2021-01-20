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

public class PluginMessageProtocol<T extends Plugin> {

    private final T plugin;
    private final String channel;

    private final Map<MessageDirection, PluginMessageRegistry> registries = new EnumMap<>(MessageDirection.class);

    public PluginMessageProtocol(T plugin, String channel, Consumer<PluginMessageRegistry> serverboundMessageSupplier, Consumer<PluginMessageRegistry> clientboundMessageSupplier) {
        this.plugin = plugin;
        this.channel = channel;

        this.constructAndRegisterRegistry(MessageDirection.SERVERBOUND, serverboundMessageSupplier);
        this.constructAndRegisterRegistry(MessageDirection.CLIENTBOUND, clientboundMessageSupplier);
    }

    public T getPlugin() {
        return plugin;
    }

    public String getChannel() {
        return channel;
    }

    public void sendMessage(Player player, PluginMessage<T> message) {
        int messageId = getPluginMessageId(MessageDirection.CLIENTBOUND, message);
        if (messageId < 0) {
            throw new IllegalStateException("Invalid plugin message, " + message.getClass().getName() + ". Is it registered?");
        }

        PluginMessageByteBuffer buffer = new PluginMessageByteBuffer();
        buffer.writeVarInt(messageId);
        message.write(buffer);

        player.sendPluginMessage(plugin, channel, buffer.asByteArray());
    }

    public int getPluginMessageId(MessageDirection direction, PluginMessage<T> message) {
        return getPluginMessageId(direction, message.getClass());
    }

    public int getPluginMessageId(MessageDirection direction, Class<?> messageClass) {
        return registries.get(direction).messageIds.get(messageClass);
    }

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

    public class PluginMessageRegistry {

        private final Map<Class<? extends PluginMessage<T>>, Integer> messageIds = new IdentityHashMap<>();
        private final List<Supplier<? extends PluginMessage<T>>> messageConstructors = new ArrayList<>();

        private PluginMessageRegistry() { }

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
