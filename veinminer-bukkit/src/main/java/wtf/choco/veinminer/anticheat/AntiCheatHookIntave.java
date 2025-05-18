package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import de.jpx3.intave.access.check.Check;
import de.jpx3.intave.access.check.event.IntaveViolationEvent;
import de.jpx3.intave.access.check.event.IntaveViolationEvent.Reaction;

/**
 * The default Intave AntiCheat hook implementation.
 */
public final class AntiCheatHookIntave implements AntiCheatHook, Listener {

    private final Set<UUID> exempt = new HashSet<>();

    @Override
    public void exempt(@NotNull Player player) {
        this.exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        this.exempt.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

    @EventHandler
    private void onIntaveViolation(IntaveViolationEvent event) {
        if (!exempt.contains(event.player().getUniqueId())) {
            return;
        }

        Check check = event.checkEnum();
        if (check == Check.BREAK_SPEED_LIMITER || check == Check.INTERACTION_RAYTRACE) {
            event.suggestReaction(Reaction.IGNORE);
        }
    }

}
