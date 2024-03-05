package wtf.choco.veinminer.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link SimpleEconomy} with no affect on the player.
 * Players will never require money to be withdrawn.
 */
public final class EmptyEconomy implements SimpleEconomy {

    /**
     * The singleton instance of the {@link EmptyEconomy}.
     */
    public static final SimpleEconomy INSTANCE = new EmptyEconomy();

    private EmptyEconomy() { }

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

    @Override
    public int getFractionalDigits() {
        return 0;
    }

}
