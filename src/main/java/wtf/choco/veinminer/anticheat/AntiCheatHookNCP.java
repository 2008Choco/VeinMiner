package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * The default NoCheatPlus (NCP) hook implementation
 */
public class AntiCheatHookNCP implements AntiCheatHook {

	private final Set<UUID> exempted = new HashSet<>();

	@Override
	public String getPluginName() {
		return "NCP";
	}

	@Override
	public void exempt(Player player) {
		if (!NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK)) return;

		NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK);
		this.exempted.add(player.getUniqueId());
	}

	@Override
	public void unexempt(Player player) {
		if (!exempted.contains(player.getUniqueId())) return;
		NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK);
	}

	@Override
	public boolean shouldUnexempt(Player player) {
		return exempted.contains(player.getUniqueId());
	}

}