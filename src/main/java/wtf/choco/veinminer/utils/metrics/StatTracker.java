package wtf.choco.veinminer.utils.metrics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;

/**
 * Represents a statistic tracker for the bStats {@link Metrics} class. Any temporary data to do
 * with bStats custom charts will be calculated here.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class StatTracker {

    private static StatTracker instance;

    private final Map<Material, Integer> minedBlocks = new EnumMap<>(Material.class);

    private StatTracker() { }

    /**
     * Add one to the amount of mined blocks for the provided material.
     *
     * @param material the material to accumulate
     */
    public void accumulateVeinMinedMaterial(Material material) {
        this.minedBlocks.merge(material, 1, Integer::sum);
    }

    /**
     * Get the data as a {@literal Map<String, Integer>} for bStats. Note that the invocation of
     * this method will result in previous data being cleared and reset back to 0. This should ONLY
     * be invoked by bStats data collectors as to not mess up existing data.
     *
     * @return the readable bStats data
     */
    public Map<String, Integer> getVeinMinedCountAsData() {
        Map<String, Integer> data = new HashMap<>();

        this.minedBlocks.forEach((k, v) -> data.put(k.getKey().toString(), v));
        this.minedBlocks.clear();

        return data;
    }

    /**
     * Get a singleton instance of the StatTracker.
     *
     * @return the stat tracker instance
     */
    public static StatTracker get() {
        return (instance == null) ? instance = new StatTracker() : instance;
    }

}