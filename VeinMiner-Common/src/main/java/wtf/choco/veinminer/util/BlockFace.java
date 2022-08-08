package wtf.choco.veinminer.util;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a block face.
 */
public enum BlockFace {

    /**
     * North (z -1).
     */
    NORTH(0, 0, -1),
    /**
     * East (x +1).
     */
    EAST(1, 0, 0),
    /**
     * South (z +1).
     */
    SOUTH(0, 0, 1),
    /**
     * West (x -1).
     */
    WEST(-1, 0, 0),
    /**
     * Up (y +1).
     */
    UP(0, 1, 0),
    /**
     * Down (y -1).
     */
    DOWN(0, -1, 0),

    /**
     * North east (x +1, z -1).
     */
    NORTH_EAST(1, 0, -1),
    /**
     * North west (x -1, z -1).
     */
    NORTH_WEST(-1, 0, -1),
    /**
     * South east (x +1, z +1).
     */
    SOUTH_EAST(1, 0, 1),
    /**
     * South west (x -1, z +1).
     */
    SOUTH_WEST(-1, 0, 1),

    /**
     * North up (y +1, z -1).
     */
    NORTH_UP(0, 1, -1),
    /**
     * East up (x +1, y +1).
     */
    EAST_UP(1, 1, 0),
    /**
     * South up (y +1, z +1).
     */
    SOUTH_UP(0, 1, 1),
    /**
     * West up (x -1, y +1).
     */
    WEST_UP(-1, 1, 0),
    /**
     * North down (y -1, z -1).
     */
    NORTH_DOWN(0, -1, -1),
    /**
     * East down (x +1, y -1).
     */
    EAST_DOWN(1, -1, 0),
    /**
     * South down (y -1, z +1).
     */
    SOUTH_DOWN(0, -1, 1),
    /**
     * West down (x -1, y -1).
     */
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
     * Get the {@link BlockFace} opposite to this BlockFace. Note that this works only for
     * cardinal faces (up, down, east, west, north, and south). Any other face will throw
     * an {@link UnsupportedOperationException}.
     *
     * @return the opposite block face
     */
    @NotNull
    public BlockFace getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            default -> throw new UnsupportedOperationException("Unknown opposing BlockFace");
        };
    }

    /**
     * Get a {@link BlockPosition} offset by this {@link BlockFace}'s offset coordinates
     * relative to the given BlockPosition.
     *
     * @param position the block of reference
     *
     * @return the relative block
     */
    @NotNull
    public BlockPosition getRelative(@NotNull BlockPosition position) {
        return position.offset(xOffset, yOffset, zOffset);
    }

}
