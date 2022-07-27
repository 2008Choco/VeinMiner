package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.ApiStatus.Internal;

import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.api.event.player.PatternChangeEvent;
import wtf.choco.veinminer.pattern.VeinMiningPattern;

@Internal
public interface VeinMinerEventDispatcher {

    public PatternChangeEvent callPatternChangeEvent(VeinMinerPlayer player, VeinMiningPattern pattern, VeinMiningPattern newPattern, PatternChangeEvent.Cause cause);

    public boolean handleClientActivateVeinMinerEvent(VeinMinerPlayer player, boolean activated);

}
