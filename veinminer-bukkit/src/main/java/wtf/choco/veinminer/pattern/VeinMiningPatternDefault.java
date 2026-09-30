package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMiningConfiguration;

/**
 * The default {@link VeinMiningPattern} that mines as many blocks in an arbitrary pattern
 * as possible.
 */
public final class VeinMiningPatternDefault implements VeinMiningPattern {

    private static final VeinMiningPattern INSTANCE = new VeinMiningPatternDefault();
    private static final NamespacedKey KEY = VeinMinerPlugin.key("default");

    private VeinMiningPatternDefault() { }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public List<Block> allocateBlocks(@NotNull Block origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfiguration config, @Nullable BlockList aliasList) {
        List<Block> blocks = new ArrayList<>();
        List<Block> frontier = new ArrayList<>(32);
        List<Block> nextFrontier = new ArrayList<>(32);
        Set<Block> visited = new HashSet<>();
        frontier.add(origin);
        visited.add(origin);

        int maxVeinSize = config.getMaxVeinSize();
        BlockData originBlockData = origin.getBlockData();

        // Such loops, much wow! I promise, this is as efficient as it can be
        while (blocks.size() < maxVeinSize) {
            nextFrontier.clear();

            blockSearch:
            for (Block current : frontier) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            // Ignore self
                            if (x == 0 && y == 0 && z == 0) {
                                continue;
                            }

                            Block relative = current.getRelative(x, y, z);
                            if (!visited.add(relative)) {
                                continue;
                            }

                            if (!PatternUtils.typeMatches(block, aliasList, originBlockData, relative.getBlockData())) {
                                continue;
                            }

                            if (blocks.size() + nextFrontier.size() >= maxVeinSize) {
                                break blockSearch;
                            }

                            nextFrontier.add(relative);
                        }
                    }
                }
            }

            // No more blocks to allocate :D
            if (nextFrontier.isEmpty()) {
                break;
            }

            blocks.addAll(nextFrontier);
            List<Block> previousFrontier = frontier;
            frontier = nextFrontier;
            nextFrontier = previousFrontier;
        }

        return blocks;
    }

    @Nullable
    @Override
    public String getPermission() {
        return "veinminer.pattern.default";
    }

    /**
     * Get the singleton instance of {@link VeinMiningPatternDefault}.
     *
     * @return this pattern instance
     */
    @NotNull
    public static VeinMiningPattern getInstance() {
        return INSTANCE;
    }

}
