package wtf.choco.veinminer.economy;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.PlatformPlayer;

/**
 * An implementation of {@link SimpleEconomy} with no affect on the player.
 * Players will never require money to be withdrawn.
 */
public final class EmptyEconomy implements SimpleEconomy {

    public static final SimpleEconomy INSTANCE = new EmptyEconomy();

    private EmptyEconomy() { }

    @Override
    public boolean shouldCharge(@NotNull PlatformPlayer player) {
        return false;
    }

    @Override
    public boolean hasSufficientBalance(@NotNull PlatformPlayer player, double amount) {
        return true;
    }

    @Override
    public void withdraw(@NotNull PlatformPlayer player, double amount) { }

}
