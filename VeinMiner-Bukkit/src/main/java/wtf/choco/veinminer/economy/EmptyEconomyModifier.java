package wtf.choco.veinminer.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link EconomyModifier} with no affect on the player.
 * Players will never require money to be withdrawn.
 */
public final class EmptyEconomyModifier implements EconomyModifier {

    private static EmptyEconomyModifier instance;

    private EmptyEconomyModifier() { }

    @Override
    public boolean shouldCharge(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean hasSufficientBalance(@NotNull Player player, double amount) {
        return true;
    }

    @Override
    public void withdraw(@NotNull Player player, double amount) { }

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
