package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.block.BlockAccessor;
import wtf.choco.veinminer.block.BlockFace;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * The default {@link VeinMiningPattern} that mines as many blocks in an arbitrary pattern
 * as possible.
 */
public final class VeinMiningPatternDefault implements VeinMiningPattern {

    private static final NamespacedKey KEY = NamespacedKey.veinminer("default");

    private final List<BlockPosition> buffer = new ArrayList<>(32), recent = new ArrayList<>(32);

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Set<BlockPosition> allocateBlocks(@NotNull BlockAccessor blockAccessor, @NotNull BlockPosition origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMinerConfig config, @Nullable BlockList aliasList) {
        Set<BlockPosition> positions = new HashSet<>();

        this.recent.add(origin); // For first iteration

        int maxVeinSize = config.getMaxVeinSize();

        while (positions.size() < maxVeinSize) {
            recentSearch:
            for (BlockPosition current : recent) {
                for (BlockFace face : BlockFace.values()) {
                    BlockPosition relative = face.getRelative(current);
                    if (positions.contains(relative) || !PatternUtils.typeMatches(block, aliasList, blockAccessor.getState(relative))) {
                        continue;
                    }

                    if (positions.size() + buffer.size() >= maxVeinSize) {
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
            positions.addAll(buffer);

            this.buffer.clear();
        }

        this.recent.clear();

        return positions;
    }

    @Nullable
    @Override
    public String getPermission() {
        return "veinminer.pattern.default";
    }

}
