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

    private static final Map<@NotNull Material, @NotNull Integer> MINED_BLOCKS = new EnumMap<>(Material.class);
    private static final Set<@NotNull AntiCheatInformation> INSTALLED_ANTI_CHEATS = new HashSet<>(2);

    private StatTracker() { }

    /**
     * Add one to the amount of mined blocks for the provided material.
     *
     * @param material the material to accumulate
     */
    public static void accumulateVeinMinedMaterial(@NotNull Material material) {
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
    public static Map<@NotNull String, @NotNull Integer> getVeinMinedCountAsData() {
        Map<String, Integer> data = new HashMap<>();

        MINED_BLOCKS.forEach((material, amount) -> data.put(material.getKey().toString(), amount));
        MINED_BLOCKS.clear();

        return data;
    }

    /**
     * Recognize an installed anti cheat.
     *
     * @param information the anti cheat information
     */
    public static void recognizeInstalledAntiCheat(AntiCheatInformation information) {
        INSTALLED_ANTI_CHEATS.add(information);
    }

    /**
     * Get the installed anti cheat data as a {@literal Map<String, Map<String, Integer>>} for bStats.
     *
     * @return the readable bStats data
     */
    @NotNull
    public static Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull Integer>> getInstalledAntiCheatsAsData() {
        Map<String, Map<String, Integer>> data = new HashMap<>();

        INSTALLED_ANTI_CHEATS.forEach(antiCheatInformation -> {
            Map<String, Integer> versionData = new HashMap<>();
            versionData.put(antiCheatInformation.getVersion(), 1); // There will only ever be 1 installed
            data.put(antiCheatInformation.getName(), versionData);
        });

        return data;
    }

}
