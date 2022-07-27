package wtf.choco.veinminer.api.event.player;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.pattern.VeinMiningPattern;

public interface PatternChangeEvent {

    @NotNull
    public VeinMiningPattern getNewPattern();

    @NotNull
    public Cause getCause();

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
