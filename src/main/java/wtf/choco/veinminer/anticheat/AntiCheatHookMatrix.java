package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.rerere.matrix.api.events.PlayerViolationEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * The default Matrix hook implementation
 */
public final class AntiCheatHookMatrix implements AntiCheatHook, Listener {

    private final Set<UUID> exempt = new HashSet<>();

    @Override
    public String getPluginName() {
        return "Matrix";
    }

    @Override
    public void exempt(Player player) {
        this.exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(Player player) {
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
