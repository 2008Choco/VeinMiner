package wtf.choco.veinminer.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.pattern.VeinMiningPattern;

/**
 * Called when a player updates their vein mining pattern through the use of the
 * <code>/veinminer pattern</code> command.
 */
public class PlayerSwitchPatternEvent extends PlayerEvent {

    private static HandlerList handlers = new HandlerList();

    private final VeinMiningPattern oldPattern, newPattern;

    public PlayerSwitchPatternEvent(@NotNull Player player, @NotNull VeinMiningPattern oldPattern, @NotNull VeinMiningPattern newPattern) {
        super(player);

        this.oldPattern = oldPattern;
        this.newPattern = newPattern;
    }

    /**
     * Get the pattern from which the player switched.
     *
     * @return the old pattern
     */
    @NotNull
    public VeinMiningPattern getOldPattern() {
        return oldPattern;
    }

    /**
     * Get the pattern to which the player.
     *
     * @return the new pattern
     */
    @NotNull
    public VeinMiningPattern getNewPattern() {
        return newPattern;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}