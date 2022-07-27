package wtf.choco.veinminer;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the different methods of activating vein miner.
 */
public enum ActivationStrategy {

    /**
     * Never activated. Disabled.
     */
    NONE("None"),

    /**
     * Activated by the client with a client-sided mod.
     */
    CLIENT("Client"),

    /**
     * Activated when a player is holding sneak.
     */
    SNEAK("Sneak"),

    /**
     * Activated when a player is standing up (i.e. not sneaking).
     */
    STAND("Stand"),

    /**
     * Always activated.
     */
    ALWAYS("Always");

    private final String friendlyName;

    private ActivationStrategy(@NotNull String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * Get the friendly name for this activation strategy. In most cases, this is just the name
     * of the enum with more appropriate capitalization.
     *
     * @return the friendly name
     */
    @NotNull
    public String getFriendlyName() {
        return friendlyName;
    }

}
