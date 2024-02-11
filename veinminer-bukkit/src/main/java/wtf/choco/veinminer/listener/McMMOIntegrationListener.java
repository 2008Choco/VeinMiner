package wtf.choco.veinminer.listener;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

public final class McMMOIntegrationListener implements Listener {

    private final VeinMinerPlugin plugin;

    public McMMOIntegrationListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onGainVeinMinerXP(McMMOPlayerXpGainEvent event) {
        if (event.getSkill() != PrimarySkillType.MINING) {
            return;
        }

        if (!plugin.getConfiguration().isNerfMcMMO() || !event.getPlayer().hasMetadata("veinminer:veinmining")) {
            return;
        }

        event.setCancelled(true);
    }

}
