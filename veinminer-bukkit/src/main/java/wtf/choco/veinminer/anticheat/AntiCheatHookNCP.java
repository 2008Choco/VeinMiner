package wtf.choco.veinminer.anticheat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * The default NoCheatPlus (NCP) hook implementation.
 */
public final class AntiCheatHookNCP implements AntiCheatHook {

    private final Multimap<@NotNull UUID, @NotNull CheckType> exempt = ArrayListMultimap.create();
    private final VeinMinerPlugin plugin;

    public AntiCheatHookNCP(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void exempt(@NotNull Player player) {
        /*
         * Do not exempt players from checks if they already have an exemption for it (i.e. by permission).
         * This avoids the possibility of later unexempting them from things they should have exemptions for.
         *
         * Because NoCheatPlus false flags players from a variety of different check types, it's best to just
         * exempt from all checks temporarily but avoid unexempting the ones that were exempt previously.
         */

        UUID playerUUID = player.getUniqueId();

        for (CheckType check : CheckType.values()) {
            if (NCPExemptionManager.isExempted(player, check)) {
                continue;
            }

            this.exempt.put(playerUUID, check);
            NCPExemptionManager.exemptPermanently(player, check);
        }

        player.setMetadata("nocheat.exempt", new FixedMetadataValue(plugin, true));
    }

    @Override
    public void unexempt(@NotNull Player player) {
        UUID playerUUID = player.getUniqueId();
        this.exempt.get(playerUUID).forEach(check -> NCPExemptionManager.unexempt(player, check));
        this.exempt.removeAll(playerUUID);
        player.removeMetadata("nocheat.exempt", plugin);
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.containsKey(player.getUniqueId()) || player.hasMetadata("nocheat.exempt");
    }

}
