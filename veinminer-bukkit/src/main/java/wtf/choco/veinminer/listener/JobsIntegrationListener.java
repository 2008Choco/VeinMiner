package wtf.choco.veinminer.listener;

import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import com.gamingmesh.jobs.container.ActionType;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.player.VeinMinerPlayer;

public final class JobsIntegrationListener implements Listener {

    private final VeinMinerPlugin plugin;

    public JobsIntegrationListener(VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onGainVeinMinerXP(JobsExpGainEvent event) {
        if (event.getActionInfo().getType() != ActionType.BREAK) {
            return;
        }

        if (!plugin.getConfiguration().isNerfJobsExperienceGain() || isNotVeinMining(event.getPlayer().getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onGainVeinMinerCurrency(JobsPrePaymentEvent event) {
        if (event.getActionInfo().getType() != ActionType.BREAK) {
            return;
        }

        if (!plugin.getConfiguration().isNerfJobsCurrencyGain() || isNotVeinMining(event.getPlayer().getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    private boolean isNotVeinMining(@Nullable Player player) {
        if (player == null) {
            return true;
        }

        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
        return veinMinerPlayer == null || !veinMinerPlayer.isVeinMining();
    }

}
