package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The default AntiAura hook implementation
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {

    private final Set<@NotNull UUID> exempt = new HashSet<>();

    @NotNull
    @Override
    public String getPluginName() {
        return "AntiAura";
    }

    @Override
    public void exempt(@NotNull Player player) {
        if (AntiAuraAPI.API.isExemptedFromFastBreak(player)) {
            return;
        }

        if (exempt.add(player.getUniqueId())) {
            AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        }
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (exempt.remove(player.getUniqueId())) {
            AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        }
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

}
