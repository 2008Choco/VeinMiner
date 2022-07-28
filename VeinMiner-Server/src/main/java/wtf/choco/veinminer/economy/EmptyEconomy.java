package wtf.choco.veinminer.economy;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link SimpleEconomy} with no affect on the player.
 * Players will never require money to be withdrawn.
 */
public final class EmptyEconomy implements SimpleEconomy {

    public static final SimpleEconomy INSTANCE = new EmptyEconomy();

    private EmptyEconomy() { }

    @Override
    public boolean shouldCharge(@NotNull UUID playerUUID) {
        return false;
    }

    @Override
    public boolean hasSufficientBalance(@NotNull UUID playerUUID, double amount) {
        return true;
    }

    @Override
    public void withdraw(@NotNull UUID playerUUID, double amount) { }

}
