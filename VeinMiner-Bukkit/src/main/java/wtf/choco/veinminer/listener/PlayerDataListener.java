package wtf.choco.veinminer.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;

public final class PlayerDataListener implements Listener {

    private final VeinMinerPlugin plugin;

    public PlayerDataListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(@SuppressWarnings("unused") PlayerJoinEvent event) {
//        Player player = event.getPlayer();
//        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);

        // If the directory is only just created, there's no player data to read from anyways
        if (plugin.getPlayerDataDirectory().mkdirs()) {
            return;
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            // TODO: Read player state from file/database
        });
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().remove(player);

        if (veinMinerPlayer == null || !veinMinerPlayer.isDirty()) {
            return;
        }

        // TODO: Save player state to file/database
    }

}
