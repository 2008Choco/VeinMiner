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
 * A tunnel {@link VeinMiningPattern} that mines a square tunnel in a direction.
 */
public final class VeinMiningPatternTunnel implements VeinMiningPattern {

    private static final NamespacedKey KEY = VeinMinerPlugin.key("tunnel");

    private static final int DEFAULT_TUNNEL_RADIUS = 1;

    private static final BiAxisRelativeGetter RELATIVE_GETTER_NORTH_SOUTH = (position, x, y) -> position.getRelative(x, y, 0);
    private static final BiAxisRelativeGetter RELATIVE_GETTER_EAST_WEST = (position, y, z) -> position.getRelative(0, y, z);
    private static final BiAxisRelativeGetter RELATIVE_GETTER_UP_DOWN = (position, x, z) -> position.getRelative(x, 0, z);

    private final int radius;

    /**
     * Construct a new {@link VeinMiningPatternTunnel}.
     *
     * @param radius the radius of the tunnel in blocks (excluding the center block).
     */
    public VeinMiningPatternTunnel(int radius) {
        this.radius = radius;
    }

    /**
     * Construct a new {@link VeinMiningPatternTunnel} with the default tunnel radius of 1.
     */
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
    public List<Block> allocateBlocks(@NotNull Block origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfiguration config, @Nullable BlockList aliasList) {
        List<Block> positions = new ArrayList<>();
        BlockFace tunnelDirection = destroyedFace.getOppositeFace();

        BiAxisRelativeGetter relativeGetter = getRelativeGetter(tunnelDirection);
        Block currentCenter = origin;
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
        BlockData originBlockData = origin.getBlockData();

        while (maxTunnelDepth-- > 0 && calculateSquare(positions, originBlockData, currentCenter, block, aliasList, maxVeinSize, relativeGetter)) {
            currentCenter = currentCenter.getRelative(tunnelDirection);
        }

        return positions;
    }

    @Nullable
    @Override
    public String getPermission() {
        return "veinminer.pattern.tunnel";
    }

    private boolean calculateSquare(List<Block> positions, BlockData originState, Block center, VeinMinerBlock block, BlockList aliasList, int maxVeinSize, BiAxisRelativeGetter relativeGetter) {
        boolean changed = false;

        // Get the possible mod values for that axis which should be -1, 0, and 1. Or just 0 for the axis that is ignored (set to 0 above)
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                Block relative = relativeGetter.apply(center, i, j);
                if (positions.contains(relative) || !PatternUtils.typeMatches(block, aliasList, originState, relative.getBlockData())) {
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

        @NotNull
        public Block apply(@NotNull Block block, int first, int second);

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
