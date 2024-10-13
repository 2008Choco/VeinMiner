package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.frep.vulcan.api.event.VulcanFlagEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class AntiCheatHookVulcan implements AntiCheatHook, Listener {

    private final Set<UUID> exempt = new HashSet<>();

    @Override
    public void exempt(@NotNull Player player) {
        exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        exempt.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlag(VulcanFlagEvent event) {
        if (!exempt.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

}
