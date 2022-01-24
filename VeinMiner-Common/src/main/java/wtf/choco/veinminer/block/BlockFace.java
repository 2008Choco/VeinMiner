package wtf.choco.veinminer.block;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.BlockPosition;

/**
 * Represents all possible relative directions of a block supported by VeinMiner.
 */
public enum BlockFace {

    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0),

    NORTH_EAST(1, 0, -1),
    NORTH_WEST(-1, 0, -1),
    SOUTH_EAST(1, 0, 1),
    SOUTH_WEST(-1, 0, 1),

    NORTH_UP(0, 1, -1),
    EAST_UP(1, 1, 0),
    SOUTH_UP(0, 1, 1),
    WEST_UP(-1, 1, 0),
    NORTH_DOWN(0, -1, -1),
    EAST_DOWN(1, -1, 0),
    SOUTH_DOWN(0, -1, 1),
    WEST_DOWN(-1, -1, 0);

    private final int xOffset, yOffset, zOffset;

    private BlockFace(int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    /**
     * Get the x offset of this block face.
     *
     * @return the x offset
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Get the y offset of this block face.
     *
     * @return the y offset
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Get the z offset of this block face.
     *
     * @return the z offset
     */
    public int getZOffset() {
        return zOffset;
    }

    /**
     * Get the block relative to the specified block based on the current block face.
     *
     * @param position the block of reference
     *
     * @return the relative block
     */
    @NotNull
    public BlockPosition getRelative(@NotNull BlockPosition position) {
        return position.offset(xOffset, yOffset, zOffset);
    }

    /* Block views:
     *
     *       Arial:               Front:
     *
     *   NW    N    NE          WU   UP    EU
     *
     *   W   BLOCK   E          W   BLOCK   E
     *
     *   SW    S    ES          WD  DOWN   ED
     *
     */

}
