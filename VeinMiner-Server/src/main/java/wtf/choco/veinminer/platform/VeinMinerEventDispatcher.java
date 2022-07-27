package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.ApiStatus.Internal;

import wtf.choco.veinminer.api.event.player.PatternChangeEvent;
import wtf.choco.veinminer.pattern.VeinMiningPattern;

/**
 * A platform-independent event dispatcher.
 */
@Internal
public interface VeinMinerEventDispatcher {

    /**
     * Call the {@link PatternChangeEvent}.
     *
     * @param player the player that changed their pattern
     * @param pattern the player's current pattern
     * @param newPattern the new pattern
     * @param cause the cause of the switch
     *
     * @return the called pattern event
     */
    public PatternChangeEvent callPatternChangeEvent(PlatformPlayer player, VeinMiningPattern pattern, VeinMiningPattern newPattern, PatternChangeEvent.Cause cause);

    /**
     * Call and handle the client activate vein miner event.
     *
     * @param player the player that changed the vein miner activation state
     * @param activated whether or not vein miner is being activated
     *
     * @return true if the event succeeded, false if it was cancelled
     */
    public boolean handleClientActivateVeinMinerEvent(PlatformPlayer player, boolean activated);

}
