package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.system.Enums.HackType;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The default Spartan hook implementation
 */
public class AntiCheatHookSpartan implements AntiCheatHook {

	private final Set<UUID> exempted = new HashSet<>();

	@Override
	@NotNull
	public String getPluginName() {
		return "Spartan";
	}

	@Override
	public void exempt(@NotNull Player player) {
		if (API.isBypassing(player, HackType.FastBreak)) return;

		API.stopCheck(player, HackType.FastBreak);
		this.exempted.add(player.getUniqueId());
	}

	@Override
	public void unexempt(@NotNull Player player) {
		API.startCheck(player, HackType.FastBreak);
		this.exempted.remove(player.getUniqueId());
	}

	@Override
	public boolean shouldUnexempt(@NotNull Player player) {
		return exempted.contains(player.getUniqueId());
	}

}