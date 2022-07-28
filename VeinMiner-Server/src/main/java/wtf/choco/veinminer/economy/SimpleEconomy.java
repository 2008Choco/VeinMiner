package wtf.choco.veinminer.economy;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for economic transactions
 */
public interface SimpleEconomy {

    /**
     * Check whether the provided player should have money withdrawn from their
     * account before vein mining.
     *
     * @param playerUUID the UUID of the player to check
     *
     * @return true if money should be withdrawn, false otherwise
     */
    public boolean shouldCharge(@NotNull UUID playerUUID);

    /**
     * Check whether or not the provided player has a sufficient amount of money
     * to be charged.
     *
     * @param playerUUID the UUID of the player to check
     * @param amount the amount of money to withdraw
     *
     * @return true if the player has a sufficient amount of money, false otherwise
     */
    public boolean hasSufficientBalance(@NotNull UUID playerUUID, double amount);

    /**
     * Withdraw money from the specified player.
     *
     * @param playerUUID the UUID of the player from whom money should be withdrawn
     * @param amount the amount of money to withdraw
     */
    public void withdraw(@NotNull UUID playerUUID, double amount);

}
