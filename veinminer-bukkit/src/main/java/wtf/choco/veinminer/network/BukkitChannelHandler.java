package wtf.choco.veinminer.network;

import java.nio.ByteBuffer;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.platform.BukkitAdapter;
import wtf.choco.veinminer.platform.PlatformPlayer;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A {@link ChannelRegistrar} implementation for Bukkit servers.
 */
public final class BukkitChannelHandler implements ChannelRegistrar {

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link BukkitChannelHandler}.
     *
     * @param plugin the plugin instance
     */
    public BukkitChannelHandler(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerClientboundMessageHandler(@NotNull NamespacedKey channel, @NotNull PluginMessageRegistry<ClientboundPluginMessageListener> registry) {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel.toString());
    }

    @Override
    public void registerServerboundMessageHandler(@NotNull NamespacedKey channel, @NotNull PluginMessageRegistry<ServerboundPluginMessageListener> registry) {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel.toString(), (channelName, player, data) -> {
            PluginMessageByteBuffer buffer = new PluginMessageByteBuffer(ByteBuffer.wrap(data));

            try {
                int messageId = buffer.readVarInt();
                PluginMessage<ServerboundPluginMessageListener> message = registry.createPluginMessage(messageId, buffer);

                if (message == null) {
                    player.kickPlayer("Received unrecognized packet with id " + messageId + " (" + channelName + "). Contact an administrator.");
                    return;
                }

                PlatformPlayer platformPlayer = BukkitAdapter.adapt(player);
                VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(platformPlayer);
                if (veinMinerPlayer == null) {
                    return;
                }

                message.handle(veinMinerPlayer);
            } catch (IllegalStateException e) {
                player.kickPlayer("Malformatted or invalid packet (" + channelName + "). Contact an administrator.");
            }
        });
    }

}
