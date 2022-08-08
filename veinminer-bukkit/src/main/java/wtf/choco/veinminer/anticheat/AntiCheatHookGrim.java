package wtf.choco.veinminer.anticheat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import ac.grim.grimac.events.FlagEvent;

/**
 * The default Grim AntiCheat hook implementation.
 */
public final class AntiCheatHookGrim implements AntiCheatHook, Listener {

    // FlagEvent is called asynchronously, so we need a ConcurrentHashMap to be certain
    private final Set<UUID> exempt = ConcurrentHashMap.newKeySet();

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

    @EventHandler(ignoreCancelled = true)
    private void onFlag(FlagEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
