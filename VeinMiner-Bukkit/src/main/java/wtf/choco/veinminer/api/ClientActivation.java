package wtf.choco.veinminer.api;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class for client-sided integration.
 */
public final class ClientActivation {

    private static final Set<@NotNull UUID> USING_CLIENT_MOD = new HashSet<>();
    private static final Set<@NotNull UUID> CLIENT_ACTIVATED = new HashSet<>();

    private ClientActivation() { }

    /**
     * Check whether or not the given player is using the client-sided vein miner
     * mod.
     *
     * @param player the player to check
     *
     * @return true if using the mod, false otherwise
     */
    public static boolean isUsingClientMod(@NotNull Player player) {
        return USING_CLIENT_MOD.contains(player.getUniqueId());
    }

    /**
     * set whether or not the given player is using the client-sided vein miner mod.
     * This method is meant to be called internally and should not be called by
     * plugins.
     *
     * @param player the player to set
     * @param using the new use state
     */
    public static void setUsingClientMod(@NotNull Player player, boolean using) {
        if (using) {
            USING_CLIENT_MOD.add(player.getUniqueId());
        } else {
            USING_CLIENT_MOD.remove(player.getUniqueId());
        }
    }

    /**
     * Get the amount of players using the client mod.
     *
     * @return the players
     */
    public static int getPlayersUsingClientMod() {
        return USING_CLIENT_MOD.size();
    }

    /**
     * Check whether or not the player has pressed the vein miner activation key.
     *
     * @param player the player to check
     *
     * @return true if activated, false otherwise
     */
    public static boolean isActivatedOnClient(@NotNull Player player) {
        return CLIENT_ACTIVATED.contains(player.getUniqueId());
    }

    /**
     * Set whether or not the player has pressed the vein miner activation key.
     * This method is meant to be called internally and should not be called by
     * plugins.
     *
     * @param player the player to set
     * @param activated the new activation state
     */
    public static void setActivatedOnClient(@NotNull Player player, boolean activated) {
        if (activated) {
            CLIENT_ACTIVATED.add(player.getUniqueId());
        } else {
            CLIENT_ACTIVATED.remove(player.getUniqueId());
        }
    }

}
