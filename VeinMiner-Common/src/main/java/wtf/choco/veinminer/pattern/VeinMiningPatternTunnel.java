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

        /*
         * So that this can't be abused by players, the tunnel length has to be capped. Otherwise, players could
         * theoretically mine a "max vein size"-length 1x1 tunnel of blocks, which is not ideal.
         *
         * To combat this, the maximum tunnel depth should be equal to the depth you would be able to reach should
         * a perfect radius-sized tunnel be mined. For instance, with a radius of 1 and maximum vein size of 64, 9
         * blocks are mined in a single depth (a square of 3x3 blocks), meaning that the maximum tunnel depth would
         * be (max blocks / 9), or 7.1. Rounding up results is a tunnel of depth 8. Therefore, if a 1x1 tunnel were
         * mined, the maximum length can then be limited to just 8 blocks instead of 64 had it not been restricted.
         *
         * This is a decent compromise to avoid players mining 4 chunks ahead of them if the tunnel is very thin...
         */
        int blocksPerSquare = (int) Math.pow((radius * 2) + 1, 2);
        int maxTunnelDepth = (int) Math.ceil(((double) maxVeinSize) / blocksPerSquare);

        while (maxTunnelDepth-- > 0 && calculateSquare(positions, blockAccessor, currentCenter, block, aliasList, maxVeinSize, relativeGetter)) {
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
