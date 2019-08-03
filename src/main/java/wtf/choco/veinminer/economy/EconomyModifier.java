package wtf.choco.veinminer.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.data.AlgorithmConfig;

/**
 * A wrapper for economic transactions provided either by a
 * {@link VaultBasedEconomyModifier vault based economy} or a custom one supplied by
 * another plugin.
 *
 * @author Parker Hawke - 2008Choco
 */
public interface EconomyModifier {

    /**
     * Check whether the provided player should have money withdrawn from their
     * account before vein mining.
     *
     * @param player the player to check
     * @param config the relevant algorithm config (if necessary)
     *
     * @return true if money should be withdrawn, false otherwise
     */
    public boolean shouldCharge(@NotNull Player player, @NotNull AlgorithmConfig config);

    /**
     * Check whether or not the provided player has a sufficient amount of money
     * to be charged.
     *
     * @param player the player to check
     * @param config the relevant algorithm config (if necessary)
     *
     * @return true if the player has a sufficient amount of money, false otherwise
     */
    public boolean hasSufficientBalance(@NotNull Player player, @NotNull AlgorithmConfig config);

    /**
     * Charge the specified player.
     *
     * @param player the player to check
     * @param config the relevant algorithm config (if necessary)
     */
    public void charge(@NotNull Player player, @NotNull AlgorithmConfig config);

}
