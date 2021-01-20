package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.rerere.matrix.api.events.PlayerViolationEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * The default Matrix hook implementation
 */
public final class AntiCheatHookMatrix implements AntiCheatHook, Listener {

    private final Set<@NotNull UUID> exempt = new HashSet<>();

    @NotNull
    @Override
    public String getPluginName() {
        return "Matrix";
    }

    @Override
    public void exempt(@NotNull Player player) {
        this.exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        this.exempt.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onMatrixViolation(PlayerViolationEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
