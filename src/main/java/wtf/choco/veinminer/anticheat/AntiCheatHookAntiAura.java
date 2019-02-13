package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * The default AntiAura hook implementation
 */
public class AntiCheatHookAntiAura implements AntiCheatHook {

	private final double version;
	private final Set<UUID> exempted = new HashSet<>();

	public AntiCheatHookAntiAura() {
		String versionString = Bukkit.getPluginManager().getPlugin("AntiAura").getDescription().getVersion();
		this.version = NumberUtils.toDouble(versionString, -1.0);
	}

	@Override
	public String getPluginName() {
		return "AntiAura";
	}

	@Override
	public void exempt(Player player) {
		if (AntiAuraAPI.API.isExemptedFromFastBreak(player)) return;

		AntiAuraAPI.API.toggleExemptFromFastBreak(player);
		this.exempted.add(player.getUniqueId());
	}

	@Override
	public void unexempt(Player player) {
		AntiAuraAPI.API.toggleExemptFromFastBreak(player);
		this.exempted.remove(player.getUniqueId());
	}

	@Override
	public boolean shouldUnexempt(Player player) {
		return exempted.contains(player.getUniqueId());
	}

	@Override
	public boolean isSupported() {
		return version >= 10.83;
	}

}