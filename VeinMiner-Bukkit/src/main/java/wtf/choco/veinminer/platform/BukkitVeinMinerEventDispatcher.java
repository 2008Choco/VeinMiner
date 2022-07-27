package wtf.choco.veinminer.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.api.event.player.PatternChangeEvent;
import wtf.choco.veinminer.api.event.player.PatternChangeEvent.Cause;
import wtf.choco.veinminer.api.event.player.PlayerClientActivateVeinMinerEvent;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.pattern.VeinMiningPattern;

public final class BukkitVeinMinerEventDispatcher implements VeinMinerEventDispatcher {

    BukkitVeinMinerEventDispatcher() { }

    @Override
    public PatternChangeEvent callPatternChangeEvent(VeinMinerPlayer player, VeinMiningPattern pattern, VeinMiningPattern newPattern, Cause cause) {
        Player bukkitPlayer = ((BukkitPlatformPlayer) player.getPlayer()).getPlayerOrThrow();
        PlayerVeinMiningPatternChangeEvent event = new PlayerVeinMiningPatternChangeEvent(bukkitPlayer, pattern, newPattern, cause);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @Override
    public boolean handleClientActivateVeinMinerEvent(VeinMinerPlayer player, boolean activated) {
        Player bukkitPlayer = ((BukkitPlatformPlayer) player.getPlayer()).getPlayerOrThrow();
        PlayerClientActivateVeinMinerEvent event = new PlayerClientActivateVeinMinerEvent(bukkitPlayer, activated);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

}
