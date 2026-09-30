package wtf.choco.veinminer.pattern;

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
import wtf.choco.veinminer.util.BreadthFirstExpansion;

import java.util.List;

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
        int maxVeinSize = config.getMaxVeinSize();
        BlockData originBlockData = origin.getBlockData();

        return BreadthFirstExpansion.expand(origin, maxVeinSize, (current, visitor) -> {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        if (!visitor.test(current.getRelative(x, y, z))) {
                            return;
                        }
                    }
                }
            }
        }, candidate -> PatternUtils.typeMatches(block, aliasList, originBlockData, candidate.getBlockData()));
    }

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
