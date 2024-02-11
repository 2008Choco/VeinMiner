package wtf.choco.veinminer.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * Add one to the amount of mined blocks for the provided block type.
     *
     * @param blockType the block type to accumulate
     */
    public static void accumulateVeinMinedMaterial(@NotNull Material blockType) {
        MINED_BLOCKS.merge(blockType, 1, Integer::sum);
    }

    /**
     * Get the vein mined block data as a {@literal Map<String, Integer>} for bStats. Note that the
     * invocation of this method will result in previous data being cleared and reset back to 0. This
     * should ONLY be invoked by bStats data collectors as to not mess up existing data.
     *
     * @return the readable bStats data
     */
    @NotNull
    public static Map<String, Integer> getVeinMinedCountAsData() {
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
    public static void recognizeInstalledAntiCheat(@NotNull AntiCheat antiCheat) {
        INSTALLED_ANTI_CHEATS.add(antiCheat);
    }

    /**
     * Get the installed anti cheat data as a {@literal Map<String, Map<String, Integer>>} for bStats.
     *
     * @return the readable bStats data
     */
    @NotNull
    public static Map<String, Map<String, Integer>> getInstalledAntiCheatsAsData() {
        Map<String, Map<String, Integer>> data = new HashMap<>();

        INSTALLED_ANTI_CHEATS.forEach(antiCheat -> {
            Map<String, Integer> versionData = new HashMap<>();
            versionData.put(antiCheat.version(), 1); // There will only ever be 1 installed
            data.put(antiCheat.name(), versionData);
        });

        return data;
    }

}
