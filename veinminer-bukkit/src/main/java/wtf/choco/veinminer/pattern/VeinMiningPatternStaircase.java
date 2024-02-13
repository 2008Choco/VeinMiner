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
        this.key = VeinMinerPlugin.key("staircase_" + nameLowercase);
        this.permission = "veinminer.pattern.staircase_" + nameLowercase;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public List<Block> allocateBlocks(@NotNull Block origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfiguration config, @Nullable BlockList aliasList) {
        List<Block> positions = new ArrayList<>();
        BlockFace staircaseDirection = destroyedFace.getOppositeFace();

        // This can only be used on walls
        if (destroyedFace == BlockFace.UP || destroyedFace == BlockFace.DOWN) {
            return positions;
        }

        Block currentPosition = origin;
        int maxVeinSize = config.getMaxVeinSize();
        BlockData originBlockData = origin.getBlockData();

        while (calculateStairSegment(positions, originBlockData, currentPosition, block, aliasList, maxVeinSize)) {
            currentPosition = currentPosition.getRelative(staircaseDirection.getModX(), direction.getModY(), staircaseDirection.getModZ());
        }

        return positions;
    }

    @Nullable
    @Override
    public String getPermission() {
        return permission;
    }

    private boolean calculateStairSegment(List<Block> positions, BlockData originBlockData, Block current, VeinMinerBlock block, BlockList aliasList, int maxVeinSize) {
        boolean changed = false, interrupted = false;

        for (int y = -1; y <= 1; y++) {
            Block relative = current.getRelative(0, y, 0);
            if (positions.contains(relative) || !PatternUtils.typeMatches(block, aliasList, originBlockData, relative.getBlockData())) {
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
