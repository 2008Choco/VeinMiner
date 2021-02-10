package wtf.choco.veinminer.integration;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.utils.VMConstants;

public final class McMMOIntegration implements Listener {

    private final VeinMiner plugin;

    public McMMOIntegration(VeinMiner plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onGainVeinMinerXP(McMMOPlayerXpGainEvent event) {
        if (event.getSkill() != PrimarySkillType.MINING) {
            return;
        }

        if (!plugin.getConfig().getBoolean(VMConstants.CONFIG_NERF_MCMMO, false) || !event.getPlayer().hasMetadata("veinminer:veinmining")) {
            return;
        }

        event.setCancelled(true);
    }

}
