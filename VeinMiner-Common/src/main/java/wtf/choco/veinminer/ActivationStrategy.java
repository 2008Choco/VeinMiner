package wtf.choco.veinminer;

/**
 * Represents the different methods of activating VeinMiner.
 */
public enum ActivationStrategy {

    /**
     * Never activated. Disabled.
     */
    NONE,

    /**
     * Activated by the client with the client-sided mod to allow players to use their
     * own key binds.
     */
    CLIENT,

    /**
     * Activated when a Player is holding sneak.
     */
    SNEAK,

    /**
     * Activated when a Player is standing up (i.e. not sneaking).
     */
    STAND,

    /**
     * Always activated.
     */
    ALWAYS;

}
