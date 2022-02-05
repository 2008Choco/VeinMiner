package wtf.choco.veinminer.pattern;

import java.util.HashSet;
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

public final class VeinMiningPatternTunnel implements VeinMiningPattern {

    private static final NamespacedKey KEY = NamespacedKey.veinminer("tunnel");

    private static final int DEFAULT_TUNNEL_RADIUS = 1;

    private static final BiAxisRelativeGetter RELATIVE_GETTER_NORTH_SOUTH = (position, x, y) -> position.offset(x, y, 0);
    private static final BiAxisRelativeGetter RELATIVE_GETTER_EAST_WEST = (position, y, z) -> position.offset(0, y, z);
    private static final BiAxisRelativeGetter RELATIVE_GETTER_UP_DOWN = (position, x, z) -> position.offset(x, 0, z);

    private final int radius;

    public VeinMiningPatternTunnel(int radius) {
        this.radius = radius;
    }

    public VeinMiningPatternTunnel() {
        this(DEFAULT_TUNNEL_RADIUS);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Set<BlockPosition> allocateBlocks(@NotNull BlockAccessor blockAccessor, @NotNull BlockPosition origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMinerConfig config, @Nullable BlockList aliasList) {
        Set<BlockPosition> positions = new HashSet<>();
        BlockFace tunnelDirection = destroyedFace.getOpposite();

        BiAxisRelativeGetter relativeGetter = getRelativeGetter(tunnelDirection);
        BlockPosition currentCenter = origin;
        int maxVeinSize = config.getMaxVeinSize();

        while (calculateSquare(positions, blockAccessor, currentCenter, block, aliasList, maxVeinSize, relativeGetter)) {
            currentCenter = currentCenter.getRelative(tunnelDirection);
        }

        return positions;
    }

    private boolean calculateSquare(Set<BlockPosition> positions, BlockAccessor blockAccessor, BlockPosition center, VeinMinerBlock block, BlockList aliasList, int maxVeinSize, BiAxisRelativeGetter relativeGetter) {
        boolean changed = false;

        // Get the possible mod values for that axis which should be -1, 0, and 1. Or just 0 for the axis that is ignored (set to 0 above)
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                BlockPosition relative = relativeGetter.apply(center, i, j);
                if (positions.contains(relative) || !PatternUtils.typeMatches(block, aliasList, blockAccessor.getState(relative))) {
                    continue;
                }

                changed |= positions.add(relative);

                if (positions.size() >= maxVeinSize) {
                    return false;
                }
            }
        }

        return changed;
    }

    @FunctionalInterface
    private interface BiAxisRelativeGetter {

        public BlockPosition apply(BlockPosition position, int first, int second);

    }

    private static BiAxisRelativeGetter getRelativeGetter(BlockFace face) {
        return switch (face) {
            case NORTH, SOUTH -> RELATIVE_GETTER_NORTH_SOUTH;
            case EAST, WEST -> RELATIVE_GETTER_EAST_WEST;
            case UP, DOWN -> RELATIVE_GETTER_UP_DOWN;
            default -> throw new UnsupportedOperationException("Not a cardinal direction");
        };
    }

}
