package wtf.choco.veinminer.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper for economic transactions.
 */
public interface SimpleEconomy {

    /**
     * Check whether the provided player should have money withdrawn from their
     * account before vein mining.
     *
     * @param player the player to check
     *
     * @return true if money should be withdrawn, false otherwise
     */
    public boolean shouldCharge(@NotNull Player player);

    /**
     * Check whether or not the provided player has a sufficient amount of money
     * to be charged.
     *
     * @param player the the player to check
     * @param amount the amount of money to withdraw
     *
     * @return true if the player has a sufficient amount of money, false otherwise
     */
    public boolean hasSufficientBalance(@NotNull Player player, double amount);

    /**
     * Withdraw money from the specified player.
     *
     * @param player the player from whom money should be withdrawn
     * @param amount the amount of money to withdraw
     */
    public void withdraw(@NotNull Player player, double amount);

    /**
     * Get the amount of digits to show in the fractional portion of this economy.
     *
     * @return the fractional digit count, or -1 if no rounding occurs
     */
    public int getFractionalDigits();

}
