package wtf.choco.veinminer.economy;

import com.google.common.base.Preconditions;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.VMConstants;

/**
 * An implementation of {@link SimpleEconomy} to make use of a Vault-supported
 * economy plugin.
 */
public class SimpleVaultEconomy implements SimpleEconomy {

    private final Economy economy;

    /**
     * Construct a new {@link SimpleVaultEconomy}.
     */
    public SimpleVaultEconomy() {
        Preconditions.checkArgument(Bukkit.getPluginManager().getPlugin("Vault") != null, "Vault must be loaded in order to use a SimpleVaultEconomy");

        RegisteredServiceProvider<@NotNull Economy> serviceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        this.economy = (serviceProvider != null) ? serviceProvider.getProvider() : null;
    }

    @Override
    public boolean shouldCharge(@NotNull UUID playerUUID) {
        if (!hasEconomyPlugin()) {
            return false;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        return player != null && !player.hasPermission(VMConstants.PERMISSION_FREE_ECONOMY);
    }

    @Override
    public boolean hasSufficientBalance(@NotNull UUID playerUUID, double amount) {
        if (!hasEconomyPlugin()) {
            return true;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        return player != null && economy.has(player, amount);
    }

    @Override
    public void withdraw(@NotNull UUID playerUUID, double amount) {
        Preconditions.checkArgument(playerUUID != null, "playerUUID must not be null");

        Player player = Bukkit.getPlayer(playerUUID);
        Preconditions.checkState(player != null, "cannot charge offline player");

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
        return economy != null;
    }

}
