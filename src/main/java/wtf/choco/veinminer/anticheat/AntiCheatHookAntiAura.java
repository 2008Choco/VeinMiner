package wtf.choco.veinminer.anticheat;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * The default AntiAura hook implementation
 */
public class AntiCheatHookAntiAura implements AntiCheatHook {

	private double version = -1.0;

	public AntiCheatHookAntiAura() {
		String versionString = Bukkit.getPluginManager().getPlugin("AntiAura").getDescription().getVersion();
		this.version = NumberUtils.toDouble(versionString);
	}

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
		return version >= 10.83;
	}

}