package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.vagdedes.spartan.api.PlayerViolationEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * The default Spartan hook implementation.
 */
public final class AntiCheatHookSpartan implements AntiCheatHook, Listener {

    private boolean supported;

    private final Set<UUID> exempt = new HashSet<>();

    public AntiCheatHookSpartan(@NotNull VeinMinerPlugin plugin) {
        try {
            Class.forName("me.vagdedes.spartan.api.PlayerViolationEvent");
            this.supported = true;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("The version of Spartan on this server is incompatible with VeinMiner. Please post information on the spigot resource discussion page.");
        }
    }

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

    @Override
    public boolean isSupported() {
        return supported;
    }

    @EventHandler
    private void onViolation(PlayerViolationEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
