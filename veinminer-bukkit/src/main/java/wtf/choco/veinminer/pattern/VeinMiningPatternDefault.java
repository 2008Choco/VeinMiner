package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMiningConfiguration;

/**
 * The default {@link VeinMiningPattern} that mines as many blocks in an arbitrary pattern
 * as possible.
 */
public final class VeinMiningPatternDefault implements VeinMiningPattern {

    private static final VeinMiningPattern INSTANCE = new VeinMiningPatternDefault();
    private static final NamespacedKey KEY = NamespacedKey.of("veinminer", "default");

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

        while (blocks.size() < maxVeinSize) {
            recentSearch:
            for (Block current : recent) {
                for (BlockFace face : BlockFace.values()) { // TODO: Use better faces
                    Block relative = current.getRelative(face);
                    if (blocks.contains(relative) || !PatternUtils.typeMatches(block, aliasList, originBlockData, relative.getBlockData())) {
                        continue;
                    }

                    if (blocks.size() + buffer.size() >= maxVeinSize) {
                        break recentSearch;
                    }

                    this.buffer.add(relative);
                }
            }

            // No more positions to allocate :D
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