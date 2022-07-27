package wtf.choco.veinminer.api.event.player;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.pattern.VeinMiningPattern;

/**
 * A callable event when a player changes their pattern.
 */
public interface PatternChangeEvent {

    /**
     * Get the {@link VeinMiningPattern} to change to.
     *
     * @return the new pattern
     */
    @NotNull
    public VeinMiningPattern getNewPattern();

    /**
     * Get the {@link PatternChangeEvent.Cause Cause} of this event.
     *
     * @return the cause
     */
    @NotNull
    public Cause getCause();

    /**
     * Checks whether or not this event has been cancelled.
     *
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled();

    /**
     * The cause for a {@link PatternChangeEvent} to be called.
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
