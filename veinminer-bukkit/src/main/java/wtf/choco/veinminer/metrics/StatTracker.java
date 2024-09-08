package wtf.choco.veinminer.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * A general purpose metric tracker.
 */
public final class StatTracker {

    private static final Map<Material, Integer> MINED_BLOCKS = new HashMap<>();
    private static final Set<AntiCheat> INSTALLED_ANTI_CHEATS = new HashSet<>(2); // Using a Set in the rare case that a server has two anti cheats installed

    private StatTracker() { }

    /**
     * Setup custom charts on the given Metrics instance using data provided by the StatTracker.
     *
     * @param metrics the metrics instance to setup
     */
    public static void setupMetrics(@NotNull Metrics metrics) {
        metrics.addCustomChart(new AdvancedPie("blocks_veinmined", StatTracker::pollMinedBlocks));
        metrics.addCustomChart(new DrilldownPie("installed_anticheats", StatTracker::pollInstalledAntiCheats));
    }

    /**
     * Add one to the amount of mined blocks for the provided block type.
     *
     * @param material the material to accumulate
     */
    public static void incrementMinedBlock(@NotNull Material material) {
        MINED_BLOCKS.merge(material, 1, Integer::sum);
    }

    /**
     * Get the vein mined block data as a {@literal Map<String, Integer>} for bStats. Note that the
     * invocation of this method will result in previous data being cleared and reset back to 0. This
     * should ONLY be invoked by bStats data collectors as to not mess up existing data.
     *
     * @return the readable bStats data
     */
    @NotNull
    private static Map<String, Integer> pollMinedBlocks() {
        Map<String, Integer> data = new HashMap<>();

        MINED_BLOCKS.forEach((blockType, amount) -> data.put(blockType.getKey().toString(), amount));
        MINED_BLOCKS.clear();

        return data;
    }

    /**
     * Recognize an installed anti cheat.
     *
     * @param antiCheat the anti cheat information
     */
    public static void addInstalledAntiCheat(@NotNull AntiCheat antiCheat) {
        INSTALLED_ANTI_CHEATS.add(antiCheat);
    }

    /**
     * Get the installed anti cheat data as a {@literal Map<String, Map<String, Integer>>} for bStats.
     *
     * @return the readable bStats data
     */
    @NotNull
    private static Map<String, Map<String, Integer>> pollInstalledAntiCheats() {
        Map<String, Map<String, Integer>> data = new HashMap<>();

        INSTALLED_ANTI_CHEATS.forEach(antiCheat -> {
            Map<String, Integer> versionData = new HashMap<>();
            versionData.put(antiCheat.version(), 1); // There will only ever be 1 installed
            data.put(antiCheat.name(), versionData);
        });

        return data;
    }

}
