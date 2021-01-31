package wtf.choco.veinminer.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ClientActivation;
import wtf.choco.veinminer.data.PlayerPreferences;
import wtf.choco.veinminer.utils.MathUtil;
import wtf.choco.veinminer.utils.VMConstants;

public final class PlayerDataListener implements Listener {

    private final VeinMiner plugin;

    public PlayerDataListener(@NotNull VeinMiner plugin) {
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

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            playerData.readFromFile(plugin.getPlayerDataDirectory());

            // Notify the player of the client mod (if they're not using it)
            FileConfiguration config = plugin.getConfig();
            if (!config.getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
                return;
            }

            scheduler.runTaskLater(plugin, () -> {
                if (ClientActivation.isUsingClientMod(player)) {
                    return;
                }

                List<String> reminderMessages = config.getStringList(VMConstants.CONFIG_CLIENT_SUGGESTION_MESSAGE);
                if (reminderMessages.isEmpty()) {
                    return;
                }

                long reminderPeriod = MathUtil.parseSeconds(config.getString(VMConstants.CONFIG_CLIENT_SUGGEST_CLIENT_MOD_PERIOD, "1d"), -1) * 1000;
                if (reminderPeriod < 0) {
                    return;
                }

                long lastNotified = playerData.getLastNotifiedOfClientMod();
                long now = System.currentTimeMillis();

                if (now - lastNotified >= reminderPeriod) {
                    reminderMessages.forEach(line -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', line)));
                    playerData.setLastNotifiedOfClientMod(now);
                }
            }, 100L);
        });
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ClientActivation.setUsingClientMod(player, false);

        PlayerPreferences playerData = PlayerPreferences.get(player);
        if (!playerData.isDirty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playerData.writeToFile(plugin.getPlayerDataDirectory()));
    }

}
