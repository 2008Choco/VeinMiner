package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * The default AntiAura hook implementation
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {

    private final Set<UUID> exempted = new HashSet<>();

    @Override
    public String getPluginName() {
        return "AntiAura";
    }

    @Override
    public void exempt(Player player) {
        if (AntiAuraAPI.API.isExemptedFromFastBreak(player)) {
            return;
        }

        if (exempted.add(player.getUniqueId())) {
            AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        }
    }

    @Override
    public void unexempt(Player player) {
        if (exempted.remove(player.getUniqueId())) {
            AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        }
    }

    @Override
    public boolean shouldUnexempt(Player player) {
        return exempted.contains(player.getUniqueId());
    }

}
