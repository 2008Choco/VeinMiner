package me.choco.veinminer.anticheat;

import me.choco.veinminer.VeinMiner;

import org.bukkit.entity.Player;

/**
 * The default AntiAura hook implementation
 */
public class AntiCheatHookAntiAura implements AntiCheatHook {
	
	@Override
	public String getPluginName() {
		return "AntiAura";
	}
	
	@Override
	public void exempt(Player player) {
		AntiAuraAPI.API.toggleExemptFromFastBreak(player);
	}
	
	@Override
	public void unexempt(Player player) {
		AntiAuraAPI.API.toggleExemptFromFastBreak(player);
	}
	
	@Override
	public boolean isSupported() {
		return VeinMiner.getPlugin().getAntiAuraVersion() >= 10.83;
	}
	
}