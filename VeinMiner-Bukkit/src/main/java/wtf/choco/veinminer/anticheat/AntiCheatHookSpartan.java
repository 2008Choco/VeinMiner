package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.vagdedes.spartan.api.PlayerViolationEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * The default Spartan hook implementation
 */
public final class AntiCheatHookSpartan implements AntiCheatHook, Listener {

    private final Set<UUID> exempt = new HashSet<>();

    @Override
    public String getPluginName() {
        return "Spartan";
    }

    @Override
    public void exempt(Player player) {
        this.exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(Player player) {
        this.exempt.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(Player player) {
        return exempt.contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onMatrixViolation(PlayerViolationEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
