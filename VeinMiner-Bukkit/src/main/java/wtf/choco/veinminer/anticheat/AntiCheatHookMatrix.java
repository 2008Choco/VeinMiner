package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.rerere.matrix.api.MatrixAPIProvider;
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
        if (MatrixAPIProvider.getAPI().isBypass(player)) {
            return;
        }

        this.exempt.add(player.getUniqueId());
        MatrixAPIProvider.getAPI().setBypass(player, true);
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (exempt.remove(player.getUniqueId())) {
            MatrixAPIProvider.getAPI().setBypass(player, false);
        }
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
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
