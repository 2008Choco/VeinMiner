package wtf.choco.veinminer.network;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.ChannelRegistrar;
import wtf.choco.network.Message;
import wtf.choco.network.bukkit.BukkitChannelRegistrar;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;
import wtf.choco.veinminer.player.VeinMinerPlayer;

/**
 * A {@link ChannelRegistrar} implementation for VeinMiner on Bukkit servers.
 */
public final class VeinMinerBukkitChannelRegistrar extends BukkitChannelRegistrar<VeinMinerPlugin, VeinMinerServerboundMessageListener, VeinMinerClientboundMessageListener> {

    /**
     * Construct a new {@link VeinMinerBukkitChannelRegistrar}.
     *
     * @param plugin the plugin instance
     */
    public VeinMinerBukkitChannelRegistrar(@NotNull VeinMinerPlugin plugin) {
        super(plugin, VeinMiner.PROTOCOL);
    }

    @Override
    protected VeinMinerServerboundMessageListener onSuccessfulMessage(@NotNull Player player, @NotNull String channel, @NotNull Message<VeinMinerServerboundMessageListener> message) {
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
        return (veinMinerPlayer != null) ? veinMinerPlayer.getServerboundMessageListener() : null;
    }

}
