package wtf.choco.veinminer.anticheat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

import ac.grim.grimac.api.events.FlagEvent;

/**
 * The default Grim AntiCheat hook implementation.
 */
public final class AntiCheatHookGrim implements AntiCheatHook, Listener {

    // FlagEvent is called asynchronously, so we need a ConcurrentHashMap to be certain
    private final Set<UUID> exempt = ConcurrentHashMap.newKeySet();

    private boolean supported;

    public AntiCheatHookGrim(@NotNull VeinMinerPlugin plugin) {
        try {
            Class.forName("ac.grim.grimac.api.events.FlagEvent");
            this.supported = true;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("The version of GrimAC on this server is incompatible with Veinminer. Please post information on the spigot resource discussion page.");
            this.supported = false;
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

    @EventHandler(ignoreCancelled = true)
    private void onFlag(FlagEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
