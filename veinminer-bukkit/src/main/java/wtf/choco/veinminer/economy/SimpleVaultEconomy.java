package wtf.choco.veinminer.economy;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;

import java.util.function.Supplier;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.VMConstants;

/**
 * An implementation of {@link SimpleEconomy} to make use of a Vault-supported
 * economy plugin.
 */
public class SimpleVaultEconomy implements SimpleEconomy {

    private boolean warnedAboutMissingEconomy = false;

    private final Plugin plugin;
    private final Supplier<Economy> economy;

    /**
     * Construct a new {@link SimpleVaultEconomy}.
     *
     * @param plugin the plugin instance
     */
    public SimpleVaultEconomy(@NotNull Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");

        this.plugin = plugin;
        this.economy = Suppliers.memoize(this::getEconomy);
    }

    @Override
    public boolean shouldCharge(@NotNull Player player) {
        return economy.get() != null && !player.hasPermission(VMConstants.PERMISSION_FREE_ECONOMY);
    }

    @Override
    public boolean hasSufficientBalance(@NotNull Player player, double amount) {
        Economy economy = this.economy.get();
        return economy == null || economy.has(player, amount);
    }

    @Override
    public void withdraw(@NotNull Player player, double amount) {
        Preconditions.checkArgument(player.isOnline(), "cannot charge offline player");

        Economy economy = this.economy.get();
        if (economy != null) {
            economy.withdrawPlayer(player, amount);
        }
    }

    @Override
    public int getFractionalDigits() {
        Economy economy = this.economy.get();
        return (economy != null) ? economy.fractionalDigits() : 0;
    }

    private Economy getEconomy() {
        Economy economy = Bukkit.getServicesManager().load(Economy.class);
        if (economy == null) {
            if (!warnedAboutMissingEconomy) {
                this.plugin.getLogger().warning("Tried to use Vault economy but no economy plugin was found! You need to install an economy plugin in addition to Vault in order for this feature to work.");
                this.warnedAboutMissingEconomy = true;
            }

            return null;
        }

        return economy;
    }

}
