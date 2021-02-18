package wtf.choco.veinminer.metrics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a statistic tracker for the bStats {@link Metrics} class. Any temporary data to do
 * with bStats custom charts will be calculated here.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class StatTracker {

    private static StatTracker instance;

    private final Map<@NotNull Material, @NotNull Integer> minedBlocks = new EnumMap<>(Material.class);
    private final Set<@NotNull AntiCheatInformation> installedAntiCheats = new HashSet<>(2);

    private StatTracker() { }

    /**
     * Add one to the amount of mined blocks for the provided material.
     *
     * @param material the material to accumulate
     */
    public void accumulateVeinMinedMaterial(@NotNull Material material) {
        this.minedBlocks.merge(material, 1, Integer::sum);
    }

    /**
     * Get the vein mined block data as a {@literal Map<String, Integer>} for bStats. Note that the
     * invocation of this method will result in previous data being cleared and reset back to 0. This
     * should ONLY be invoked by bStats data collectors as to not mess up existing data.
     *
     * @return the readable bStats data
     */
    @NotNull
    public Map<@NotNull String, @NotNull Integer> getVeinMinedCountAsData() {
        Map<String, Integer> data = new HashMap<>();

        this.minedBlocks.forEach((material, amount) -> data.put(material.getKey().toString(), amount));
        this.minedBlocks.clear();

        return data;
    }

    /**
     * Recognize an installed anti cheat.
     *
     * @param information the anti cheat information
     */
    public void recognizeInstalledAntiCheat(AntiCheatInformation information) {
        this.installedAntiCheats.add(information);
    }

    /**
     * Get the installed anti cheat data as a {@literal Map<String, Map<String, Integer>>} for bStats.
     *
     * @return the readable bStats data
     */
    @NotNull
    public Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull Integer>> getInstalledAntiCheatsAsData() {
        Map<String, Map<String, Integer>> data = new HashMap<>();

        this.installedAntiCheats.forEach(antiCheatInformation -> {
            Map<String, Integer> versionData = new HashMap<>();
            versionData.put(antiCheatInformation.getVersion(), 1); // There will only ever be 1 installed
            data.put(antiCheatInformation.getName(), versionData);
        });

        return data;
    }

    /**
     * Get a singleton instance of the StatTracker.
     *
     * @return the stat tracker instance
     */
    @NotNull
    public static StatTracker get() {
        return (instance == null) ? instance = new StatTracker() : instance;
    }

}
