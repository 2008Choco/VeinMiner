package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.system.Enums.HackType;

import org.bukkit.entity.Player;

/**
 * The default Spartan hook implementation
 */
public final class AntiCheatHookSpartan implements AntiCheatHook {

    private final Set<UUID> exempted = new HashSet<>();

    @Override
    public String getPluginName() {
        return "Spartan";
    }

    @Override
    public void exempt(Player player) {
        if (API.isBypassing(player, HackType.FastBreak)) {
            return;
        }

        if (exempted.add(player.getUniqueId())) {
            API.stopCheck(player, HackType.FastBreak);
        }
    }

    @Override
    public void unexempt(Player player) {
        if (exempted.remove(player.getUniqueId())) {
            API.startCheck(player, HackType.FastBreak);
        }
    }

    @Override
    public boolean shouldUnexempt(Player player) {
        return exempted.contains(player.getUniqueId());
    }

}
