package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.konsolas.aac.api.PlayerViolationEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * The default Advanced AntiCheat (AAC) hook implementation
 */
public final class AntiCheatHookAAC implements AntiCheatHook, Listener {

    private final Set<UUID> exemptedUsers = new HashSet<>();

    @Override
    @NotNull
    public String getPluginName() {
        return "AAC";
    }

    @Override
    public void exempt(@NotNull Player player) {
        this.exemptedUsers.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        this.exemptedUsers.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAACViolation(PlayerViolationEvent event) {
        if (!exemptedUsers.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
