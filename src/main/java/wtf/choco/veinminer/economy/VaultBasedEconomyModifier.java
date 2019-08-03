package wtf.choco.veinminer.economy;

import com.google.common.base.Preconditions;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.AlgorithmConfig;

/**
 * An implementation of {@link EconomyModifier} to make use of a Vault-supported
 * economy plugin.
 *
 * @author Parker Hawke - 2008Choco
 */
public class VaultBasedEconomyModifier implements EconomyModifier {

    private final Economy economy;
    private final String bypassPermission;

    /**
     * Construct a new economy modifier with a bypass permission.
     *
     * @param bypassPermission the permission required by players to bypass economic costs
     */
    public VaultBasedEconomyModifier(@Nullable String bypassPermission) {
        Preconditions.checkArgument(Bukkit.getPluginManager().isPluginEnabled("Vault"), "Vault must be enabled in order to use a VaultBasedEconomyModifier");

        RegisteredServiceProvider<Economy> serviceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        this.economy = (serviceProvider != null) ? serviceProvider.getProvider() : null;
        this.bypassPermission = bypassPermission;
    }

    /**
     * Construct a new economy modifier without any bypass permission.
     */
    public VaultBasedEconomyModifier() {
        this(null);
    }

    @Override
    public boolean shouldCharge(Player player, AlgorithmConfig config) {
        Preconditions.checkArgument(config != null, "Must provide algorithm config");
        return economy != null && player != null && config.getCost() > 0.0 && (bypassPermission == null || !player.hasPermission(bypassPermission));
    }

    @Override
    public boolean hasSufficientBalance(Player player, AlgorithmConfig config) {
        Preconditions.checkArgument(config != null, "Must provide algorithm config");
        return economy == null || (player != null && economy.has(player, config.getCost()));
    }

    @Override
    public void charge(Player player, AlgorithmConfig config) {
        Preconditions.checkArgument(player != null, "Cannot charge null player");
        Preconditions.checkArgument(config != null, "Must provide algorithm config");

        if (economy == null) {
            return;
        }

        this.economy.withdrawPlayer(player, config.getCost());
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
