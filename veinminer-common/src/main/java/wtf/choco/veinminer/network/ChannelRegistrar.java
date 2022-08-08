package wtf.choco.veinminer.network;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * Represents a registrar capable of registering incoming and outgoing plugin message
 * channel handlers.
 * <p>
 * Some implementations of this registrar may not need to implement message handlers in one
 * of the directions. For instance, while on a Bukkit server both incoming and outgoing
 * message channels must be registered, a Fabric client need not register an outgoing message
 * channel. Implementations of this interface should register (and handle) incoming/outgoing
 * messages and delegate them to the appropriate plugin message listener for messages
 * registered to the supplied registries.
 *
 * @see PluginMessageProtocol#registerChannels(ChannelRegistrar)
 */
@OverrideOnly
public interface ChannelRegistrar {

    /**
     * Register a message handler for messages being sent from server to client.
     *
     * @param channel the channel on which to register the handler
     * @param registry the client bound plugin message registry
     */
    public void registerClientboundMessageHandler(@NotNull NamespacedKey channel, @NotNull PluginMessageRegistry<ClientboundPluginMessageListener> registry);

    /**
     * Register a message handler for messages being sent from client to server.
     *
     * @param channel the channel on which to register the handler
     * @param registry the server bound plugin message registry
     */
    public void registerServerboundMessageHandler(@NotNull NamespacedKey channel, @NotNull PluginMessageRegistry<ServerboundPluginMessageListener> registry);

}
