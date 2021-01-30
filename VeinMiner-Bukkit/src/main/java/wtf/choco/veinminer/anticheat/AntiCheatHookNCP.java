package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * The default NoCheatPlus (NCP) hook implementation
 */
public final class AntiCheatHookNCP implements AntiCheatHook {

    private final Set<@NotNull UUID> exempt = new HashSet<>();

    @NotNull
    @Override
    public String getPluginName() {
        return "NCP";
    }

    @Override
    public void exempt(@NotNull Player player) {
        if (NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK)) {
            return;
        }

        if (exempt.add(player.getUniqueId())) {
            NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK);
        }
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (exempt.remove(player.getUniqueId())) {
            NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK);
        }
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

}
