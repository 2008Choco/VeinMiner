package wtf.choco.veinminer.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;

public final class PlayerDataListener implements Listener {

    private final VeinMinerPlugin plugin;

    public PlayerDataListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.getPersistentDataStorage().load(plugin, plugin.getPlayerManager().get(event.getPlayer())).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().remove(event.getPlayer());
        if (veinMinerPlayer == null || !veinMinerPlayer.isDirty()) {
            return;
        }

        this.plugin.getPersistentDataStorage().save(plugin, veinMinerPlayer).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

}
