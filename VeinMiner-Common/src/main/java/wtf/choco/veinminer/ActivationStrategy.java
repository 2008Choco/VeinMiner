package wtf.choco.veinminer;

/**
 * Represents the different methods of activating vein miner.
 */
public enum ActivationStrategy {

    /**
     * Never activated. Disabled.
     */
    NONE,

    /**
     * Activated by the client with a client-sided mod.
     */
    CLIENT,

    /**
     * Activated when a player is holding sneak.
     */
    SNEAK,

    /**
     * Activated when a player is standing up (i.e. not sneaking).
     */
    STAND,

    /**
     * Always activated.
     */
    ALWAYS;

}
