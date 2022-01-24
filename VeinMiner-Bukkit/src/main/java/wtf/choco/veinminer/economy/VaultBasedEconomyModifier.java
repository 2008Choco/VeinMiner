package wtf.choco.veinminer.economy;

import com.google.common.base.Preconditions;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.VMConstants;

/**
 * An implementation of {@link EconomyModifier} to make use of a Vault-supported
 * economy plugin.
 */
public class VaultBasedEconomyModifier implements EconomyModifier {

    private final Economy economy;

    /**
     * Construct a new economy modifier with a bypass permission.
     */
    public VaultBasedEconomyModifier() {
        Preconditions.checkArgument(Bukkit.getPluginManager().isPluginEnabled("Vault"), "Vault must be enabled in order to use a VaultBasedEconomyModifier");

        RegisteredServiceProvider<@NotNull Economy> serviceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        this.economy = (serviceProvider != null) ? serviceProvider.getProvider() : null;
    }

    @Override
    public boolean shouldCharge(@NotNull Player player) {
        return economy != null && player != null && !player.hasPermission(VMConstants.PERMISSION_FREE_ECONOMY);
    }

    @Override
    public boolean hasSufficientBalance(@NotNull Player player, double amount) {
        return economy == null || (player != null && economy.has(player, amount));
    }

    @Override
    public void withdraw(@NotNull Player player, double amount) {
        Preconditions.checkArgument(player != null, "Cannot charge null player");

        if (economy == null) {
            return;
        }

        this.economy.withdrawPlayer(player, amount);
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
