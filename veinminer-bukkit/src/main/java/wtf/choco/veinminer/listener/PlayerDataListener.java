package wtf.choco.veinminer.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.NetworkUtil;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetPattern;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.util.VMConstants;

public final class PlayerDataListener implements Listener {

    private final VeinMinerPlugin plugin;

    public PlayerDataListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().getOrRegister(player, () -> plugin.getConfiguration().getClientConfiguration(player));

        this.plugin.getPersistentDataStorage().load(veinMinerPlayer).whenComplete((vmPlayer, e) -> {
            if (e != null) {
                e.printStackTrace();
                return;
            }

            player.setMetadata(VMConstants.METADATA_KEY_VEIN_MINER_ACTIVE, new LazyMetadataValue(plugin, CacheStrategy.NEVER_CACHE, vmPlayer::isVeinMinerActive));
            player.setMetadata(VMConstants.METADATA_KEY_VEINMINING, new LazyMetadataValue(plugin, CacheStrategy.NEVER_CACHE, vmPlayer::isVeinMining));

            // Update the selected pattern on the client
            vmPlayer.executeWhenClientIsReady(() -> vmPlayer.sendMessage(new ClientboundSetPattern(NetworkUtil.toNetwork(vmPlayer.getVeinMiningPattern().getKey()))));
        });
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().remove(event.getPlayer());
        if (veinMinerPlayer == null || !veinMinerPlayer.isDirty()) {
            return;
        }

        this.plugin.getPersistentDataStorage().save(veinMinerPlayer).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

}
