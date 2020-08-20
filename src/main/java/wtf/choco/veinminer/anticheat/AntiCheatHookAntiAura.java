package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * The default AntiAura hook implementation
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {

    private final Set<UUID> exempt = new HashSet<>();

    @Override
    public String getPluginName() {
        return "AntiAura";
    }

    @Override
    public void exempt(Player player) {
        if (AntiAuraAPI.API.isExemptedFromFastBreak(player)) {
            return;
        }

        if (exempt.add(player.getUniqueId())) {
            AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        }
    }

    @Override
    public void unexempt(Player player) {
        if (exempt.remove(player.getUniqueId())) {
            AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        }
    }

    @Override
    public boolean shouldUnexempt(Player player) {
        return exempt.contains(player.getUniqueId());
    }

}
