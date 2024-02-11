package wtf.choco.veinminer.config;

import java.util.Set;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * A configuration object holding values for the vein mining process.
 * <p>
 * Unless otherwise specified, all values obtained via this contract are updated in real
 * time and will always return the values that were last loaded into memory via
 * {@link JavaPlugin#reloadConfig()} and {@link ConfigWrapper#reload()}.
 */
public interface VeinMiningConfiguration {

    /**
     * Get the "repair friendly" value.
     * <p>
     * If vein mining is repair friendly, it signifies that vein mining should stop if
     * the tool reaches a durability of 1 so that it may be repaired. If not repair
     * friendly, vein miner will continue mining until the tool in hand breaks.
     *
     * @return true if repair friendly, false otherwise
     */
    public boolean isRepairFriendly();

    /**
     * Get the maximum amount of blocks allowed to be mined in a single vein.
     *
     * @return the maximum vein size
     */
    public int getMaxVeinSize();

    /**
     * Get the amount of money to be withdrawn from a player's bank account each time
     * they initiate a vein mine.
     *
     * @return the cost of vein mining, or 0.0 to not withdraw any currency
     */
    public double getCost();

    /**
     * Check whether or not vein miner is disabled in worlds with the given name.
     *
     * @param worldName the name of the world to check
     *
     * @return true if the world is disabled, false if allowed
     */
    public boolean isDisabledWorld(@NotNull String worldName);

    /**
     * Check whether or not vein miner is disabled in the given world.
     *
     * @param world the world to check
     *
     * @return true if the world is disabled, false if allowed
     */
    public default boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    /**
     * Get an unmodifiable {@link Set} of the names of all worlds in which vein mining
     * is disabled.
     *
     * @return the names of all worlds where vein mining is disabled
     */
    @NotNull
    @Unmodifiable
    public Set<String> getDisabledWorlds();

}
