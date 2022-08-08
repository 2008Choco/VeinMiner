package wtf.choco.veinminer.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import wtf.choco.veinminer.api.event.player.PatternChangeEvent;
import wtf.choco.veinminer.api.event.player.PatternChangeEvent.Cause;
import wtf.choco.veinminer.api.event.player.PlayerClientActivateVeinMinerEvent;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.pattern.VeinMiningPattern;

/**
 * Bukkit implementation of {@link ServerEventDispatcher}.
 */
public final class BukkitServerEventDispatcher implements ServerEventDispatcher {

    BukkitServerEventDispatcher() { }

    @Override
    public PatternChangeEvent callPatternChangeEvent(PlatformPlayer player, VeinMiningPattern pattern, VeinMiningPattern newPattern, Cause cause) {
        Player bukkitPlayer = ((BukkitPlatformPlayer) player).getPlayerOrThrow();
        PlayerVeinMiningPatternChangeEvent event = new PlayerVeinMiningPatternChangeEvent(bukkitPlayer, pattern, newPattern, cause);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @Override
    public boolean handleClientActivateVeinMinerEvent(PlatformPlayer player, boolean activated) {
        Player bukkitPlayer = ((BukkitPlatformPlayer) player).getPlayerOrThrow();
        PlayerClientActivateVeinMinerEvent event = new PlayerClientActivateVeinMinerEvent(bukkitPlayer, activated);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

}
