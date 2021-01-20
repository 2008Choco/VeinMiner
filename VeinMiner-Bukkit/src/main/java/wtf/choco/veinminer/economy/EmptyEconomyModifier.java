package wtf.choco.veinminer.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.data.AlgorithmConfig;

/**
 * An implementation of {@link EconomyModifier} with no affect on the player.
 * Players will never require money to be withdrawn.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class EmptyEconomyModifier implements EconomyModifier {

    private static EmptyEconomyModifier instance;

    private EmptyEconomyModifier() { }

    @Override
    public boolean shouldCharge(@NotNull Player player, @NotNull AlgorithmConfig config) {
        return false;
    }

    @Override
    public boolean hasSufficientBalance(@NotNull Player player, @NotNull AlgorithmConfig config) {
        return true;
    }

    @Override
    public void charge(@NotNull Player player, @NotNull AlgorithmConfig config) { }

    /**
     * Get an instance of the empty economy modifier.
     *
     * @return this instance
     */
    @NotNull
    public static EconomyModifier get() {
        return (instance != null) ? instance : (instance = new EmptyEconomyModifier());
    }

}
