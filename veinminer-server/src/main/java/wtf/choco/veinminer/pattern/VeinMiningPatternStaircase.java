package wtf.choco.veinminer.pattern;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMiningConfig;
import wtf.choco.veinminer.platform.world.BlockAccessor;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.util.BlockFace;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A staircase {@link VeinMiningPattern} that digs a 1x3 column either upwards or downwards.
 */
public final class VeinMiningPatternStaircase implements VeinMiningPattern {

    private final Direction direction;
    private final NamespacedKey key;
    private final String permission;

    /**
     * Construct a new {@link VeinMiningPatternStaircase}.
     *
     * @param direction the direction of the staircase
     */
    public VeinMiningPatternStaircase(@NotNull Direction direction) {
        String nameLowercase = direction.name().toLowerCase();

        this.direction = direction;
        this.key = NamespacedKey.veinminer("staircase_" + nameLowercase);
        this.permission = "veinminer.pattern.staircase_" + nameLowercase;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public Set<BlockPosition> allocateBlocks(@NotNull BlockAccessor blockAccessor, @NotNull BlockPosition origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfig config, @Nullable BlockList aliasList) {
        Set<BlockPosition> positions = new HashSet<>();
        BlockFace staircaseDirection = destroyedFace.getOpposite();

        // This can only be used on walls
        if (destroyedFace == BlockFace.UP || destroyedFace == BlockFace.DOWN) {
            return positions;
        }

        BlockPosition currentPosition = origin;
        int maxVeinSize = config.getMaxVeinSize();
        BlockState originState = blockAccessor.getState(origin);

        while (calculateStairSegment(positions, blockAccessor, originState, currentPosition, block, aliasList, maxVeinSize)) {
            currentPosition = currentPosition.offset(staircaseDirection.getXOffset(), direction.getModY(), staircaseDirection.getZOffset());
        }

        return positions;
    }

    @Nullable
    @Override
    public String getPermission() {
        return permission;
    }

    private boolean calculateStairSegment(Set<BlockPosition> positions, BlockAccessor blockAccessor, BlockState originState, BlockPosition currentPosition, VeinMinerBlock block, BlockList aliasList, int maxVeinSize) {
        boolean changed = false, interrupted = false;

        for (int y = -1; y <= 1; y++) {
            BlockPosition relative = currentPosition.offset(0, y, 0);
            if (positions.contains(relative) || !PatternUtils.typeMatches(block, aliasList, originState, blockAccessor.getState(relative))) {
                continue;
            }

            boolean success = positions.add(relative);
            changed |= success;
            if (!success) {
                interrupted = true;
            }

            if (positions.size() >= maxVeinSize) {
                return false;
            }
        }

        return changed && !interrupted;
    }

    /**
     * Represents the direction in which a {@link VeinMiningPatternStaircase} may mine.
     */
    public static enum Direction {

        /**
         * Upwards staircase.
         */
        UP(1),
        /**
         * Downwards staircase.
         */
        DOWN(-1);

        private final int modY;

        private Direction(int modY) {
            this.modY = modY;
        }

        /**
         * Get the modified y coordinate, either 1 or -1.
         *
         * @return the modified y coordinate
         */
        public int getModY() {
            return modY;
        }

    }

}
