package me.choco.veinminer.anticheat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * The default NoCheatPlus (NCP) hook implementation
 */
public class AntiCheatHookNCP implements AntiCheatHook {
	
	private boolean exempted = false;
	
	@Override
	public String getPluginName() {
		return "NCP";
	}
	
	@Override
	public void exempt(Player player) {
		if (!NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK)) return;
		
		NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK);
		this.exempted = true;
	}
	
	@Override
	public void unexempt(Player player) {
		if (!exempted) return;
		NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK);
	}
	
	@Override
	public boolean shouldUnexempt(Player player) {
		return exempted;
	}
	
}