package wtf.choco.veinminer.economy;

import com.google.common.base.Preconditions;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.BukkitPlatformPlayer;
import wtf.choco.veinminer.platform.PlatformPlayer;
import wtf.choco.veinminer.util.VMConstants;

/**
 * An implementation of {@link SimpleEconomy} to make use of a Vault-supported
 * economy plugin.
 */
public class SimpleVaultEconomy implements SimpleEconomy {

    private Economy economy;

    @Override
    public boolean shouldCharge(@NotNull PlatformPlayer player) {
        return hasEconomyPlugin() && !player.hasPermission(VMConstants.PERMISSION_FREE_ECONOMY);
    }

    @Override
    public boolean hasSufficientBalance(@NotNull PlatformPlayer player, double amount) {
        if (!hasEconomyPlugin()) {
            return true;
        }

        Player bukkitPlayer = ((BukkitPlatformPlayer) player).getPlayer();
        return bukkitPlayer != null && economy.has(bukkitPlayer, amount);
    }

    @Override
    public void withdraw(@NotNull PlatformPlayer player, double amount) {
        Preconditions.checkArgument(player.isOnline(), "cannot charge offline player");

        if (hasEconomyPlugin()) {
            Player bukkitPlayer = ((BukkitPlatformPlayer) player).getPlayer();
            if (bukkitPlayer != null) {
                this.economy.withdrawPlayer(bukkitPlayer, amount);
            }
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
