package wtf.choco.veinminer.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.data.PlayerPreferences;

public final class PlayerDataListener implements Listener {

    private final VeinMiner plugin;

    public PlayerDataListener(VeinMiner plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerPreferences playerData = PlayerPreferences.get(player);

        // If the directory is only just created, there's no player data to read from anyways
        if (plugin.getPlayerDataDirectory().mkdirs()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playerData.readFromFile(plugin.getPlayerDataDirectory()));
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerPreferences playerData = PlayerPreferences.get(player);

        if (!playerData.isDirty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playerData.writeToFile(plugin.getPlayerDataDirectory()));
    }

}
