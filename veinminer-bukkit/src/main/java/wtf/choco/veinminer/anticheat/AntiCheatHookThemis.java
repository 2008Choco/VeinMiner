package wtf.choco.veinminer.anticheat;

import com.gmail.olexorus.themis.api.ViolationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AntiCheatHookThemis implements AntiCheatHook, Listener {

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

    @EventHandler(ignoreCancelled = true)
    private void onViolation(ViolationEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
