package wtf.choco.veinminer.economy;

import com.google.common.base.Preconditions;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.VMConstants;

/**
 * An implementation of {@link SimpleEconomy} to make use of a Vault-supported
 * economy plugin.
 */
public class SimpleVaultEconomy implements SimpleEconomy {

    private Economy economy;

    @Override
    public boolean shouldCharge(@NotNull Player player) {
        return hasEconomyPlugin() && !player.hasPermission(VMConstants.PERMISSION_FREE_ECONOMY);
    }

    @Override
    public boolean hasSufficientBalance(@NotNull Player player, double amount) {
        if (!hasEconomyPlugin()) {
            return true;
        }

        return economy.has(player, amount);
    }

    @Override
    public void withdraw(@NotNull Player player, double amount) {
        Preconditions.checkArgument(player.isOnline(), "cannot charge offline player");

        if (hasEconomyPlugin()) {
            this.economy.withdrawPlayer(player, amount);
        }
    }

    /**
     * Check whether or not an economy implementation was found.
     *
     * @return true if economy is enabled, false otherwise
     */
    public boolean hasEconomyPlugin() {
        if (economy == null) {
            this.economy = Bukkit.getServicesManager().load(Economy.class);
        }

        return economy != null;
    }

}
