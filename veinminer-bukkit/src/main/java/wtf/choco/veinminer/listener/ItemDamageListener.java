package wtf.choco.veinminer.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.player.VeinMinerPlayer;

public final class ItemDamageListener implements Listener {

    private final VeinMinerPlugin plugin;

    public ItemDamageListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onItemDamageWhileVeinMining(PlayerItemDamageEvent event) {
        VeinMinerPlayer player = plugin.getPlayerManager().get(event.getPlayer());
        if (player == null || !player.isVeinMining()) {
            return;
        }

        if (!plugin.getConfiguration().isOnlyDamageOnFirstBlock()) {
            return;
        }

        event.setCancelled(true);
    }

}
