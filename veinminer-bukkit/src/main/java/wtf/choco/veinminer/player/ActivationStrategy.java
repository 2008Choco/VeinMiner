package wtf.choco.veinminer.player;

import com.google.common.base.Predicates;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a different method of activating vein miner.
 */
public enum ActivationStrategy implements Predicate<VeinMinerPlayer> {

    /**
     * Never activated.
     */
    NONE("None", Predicates.alwaysFalse()),

    /**
     * Activated by the client with the client-sided mod.
     */
    CLIENT("Client", VeinMinerPlayer::isClientKeyPressed),

    /**
     * Activated when a player is sneaking.
     */
    SNEAK("Sneak", player -> player.getPlayer().isSneaking()),

    /**
     * Activated when a player is standing up (i.e. not sneaking).
     */
    STAND("Stand", player -> !player.getPlayer().isSneaking()),

    /**
     * Always activated.
     */
    ALWAYS("Always", Predicates.alwaysTrue());

    private final String friendlyName;
    private final Predicate<VeinMinerPlayer> predicate;

    private ActivationStrategy(@NotNull String friendlyName, @NotNull Predicate<VeinMinerPlayer> predicate) {
        this.friendlyName = friendlyName;
        this.predicate = predicate;
    }

    /**
     * Get the friendly name for this activation strategy. This is just the name of the enum
     * with more appropriate capitalization.
     *
     * @return the friendly name
     */
    @NotNull
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Check whether or not the given player has activated this strategy.
     *
     * @param player the player
     *
     * @return true if active, false otherwise
     */
    @Override
    public boolean test(VeinMinerPlayer player) {
        return predicate.test(player);
    }

    /**
     * Check whether or not the given player has activated this strategy.
     * <p>
     * This method exists purely for the sake of clarity and simply delegates to
     * {@link #test(VeinMinerPlayer)}.
     *
     * @param player the player
     *
     * @return true if active, false otherwise
     */
    public boolean isActive(@NotNull VeinMinerPlayer player) {
        return test(player);
    }

}
