package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.List;

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

    private final List<Block> buffer = new ArrayList<>(32), recent = new ArrayList<>(32);

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

        this.recent.add(origin); // For first iteration

        int maxVeinSize = config.getMaxVeinSize();
        BlockData originBlockData = origin.getBlockData();

        // Such loops, much wow! I promise, this is as efficient as it can be
        while (blocks.size() < maxVeinSize) {
            recentSearch:
            for (Block current : recent) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            // Ignore self
                            if (x == 0 && y == 0 && z == 0) {
                                continue;
                            }

                            Block relative = current.getRelative(x, y, z);
                            if (blocks.contains(relative) || buffer.contains(relative)) {
                                continue;
                            }

                            if (!PatternUtils.typeMatches(block, aliasList, originBlockData, relative.getBlockData())) {
                                continue;
                            }

                            if (blocks.size() + buffer.size() >= maxVeinSize) {
                                break recentSearch;
                            }

                            this.buffer.add(relative);
                        }
                    }
                }
            }

            // No more blocks to allocate :D
            if (buffer.isEmpty()) {
                break;
            }

            this.recent.clear();
            this.recent.addAll(buffer);
            blocks.addAll(buffer);

            this.buffer.clear();
        }

        this.recent.clear();

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
