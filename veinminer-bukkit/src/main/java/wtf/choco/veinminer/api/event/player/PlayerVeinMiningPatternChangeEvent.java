package wtf.choco.veinminer.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.pattern.VeinMiningPattern;

/**
 * Called when a {@link Player} changes their {@link VeinMiningPattern} either by command
 * or with the client-sided mod.
 */
public final class PlayerVeinMiningPatternChangeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final VeinMiningPattern pattern;
    private VeinMiningPattern newPattern;
    private final Cause cause;

    /**
     * Construct a new {@link PlayerVeinMiningPatternChangeEvent}.
     *
     * @param player the player whose pattern was changed
     * @param pattern the player's current pattern
     * @param newPattern the pattern to be set
     * @param cause the cause of this event
     */
    public PlayerVeinMiningPatternChangeEvent(@NotNull Player player, @NotNull VeinMiningPattern pattern, @NotNull VeinMiningPattern newPattern, @NotNull Cause cause) {
        super(player);

        this.pattern = pattern;
        this.newPattern = newPattern;
        this.cause = cause;
    }

    /**
     * Get the player's current {@link VeinMiningPattern}.
     *
     * @return the current pattern
     */
    @NotNull
    public VeinMiningPattern getPattern() {
        return pattern;
    }

    /**
     * Set the {@link VeinMiningPattern} to change to.
     *
     * @param newPattern the new pattern
     */
    public void setNewPattern(@NotNull VeinMiningPattern newPattern) {
        this.newPattern = newPattern;
    }

    /**
     * Get the {@link VeinMiningPattern} to change to.
     *
     * @return the new pattern
     */
    @NotNull
    public VeinMiningPattern getNewPattern() {
        return newPattern;
    }

    /**
     * Get the {@link Cause} of this event.
     *
     * @return the cause
     */
    @NotNull
    public Cause getCause() {
        return cause;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * The cause for a {@link PlayerVeinMiningPatternChangeEvent} to be called.
     */
    public static enum Cause {

        /**
         * The pattern was changed as a result of a command.
         */
        COMMAND,

        /**
         * The pattern was changed per request of the client-sided mod using a key bind.
         */
        CLIENT;

    }

}
