package wtf.choco.veinminer.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetPattern;
import wtf.choco.veinminer.platform.BukkitServerPlatform;
import wtf.choco.veinminer.platform.PlatformPlayer;

public final class PlayerDataListener implements Listener {

    private final VeinMinerPlugin plugin;

    public PlayerDataListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();
        PlatformPlayer platformPlayer = BukkitServerPlatform.getInstance().getPlatformPlayer(bukkitPlayer.getUniqueId());
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().getOrRegister(platformPlayer, () -> VeinMinerPlugin.createClientConfig(bukkitPlayer));

        VeinMinerServer.getInstance().getPersistentDataStorage().load(veinMinerPlayer).whenComplete((player, e) -> {
            if (e != null) {
                e.printStackTrace();
                return;
            }

            // Update the selected pattern on the client
            player.executeWhenClientIsReady(() -> VeinMiner.PROTOCOL.sendMessageToClient(player, new PluginMessageClientboundSetPattern(player.getVeinMiningPattern().getKey())));
        });
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        PlatformPlayer platformPlayer = BukkitServerPlatform.getInstance().getPlatformPlayer(event.getPlayer().getUniqueId());
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().remove(platformPlayer);
        if (veinMinerPlayer == null || !veinMinerPlayer.isDirty()) {
            return;
        }

        VeinMinerServer.getInstance().getPersistentDataStorage().save(veinMinerPlayer).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

}
