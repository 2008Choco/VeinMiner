package wtf.choco.veinminer.util;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a set of x, y, and z coordinates in a world.
 *
 * @param x the x coordinate
 * @param y the y coordinate
 * @param z the z coordinate
 */
public record BlockPosition(int x, int y, int z) {

    private static final double MAXIMUM_UPPER_BOUND = Math.pow(2, 25), NEGATIVE_UPPER_BOUND = Math.pow(2, 26);
    private static final double MAXIMUM_UPPER_BOUND_Y = Math.pow(2, 11), NEGATIVE_UPPER_BOUND_Y = Math.pow(2, 12);

    /**
     * Get a new {@link BlockPosition} offset by the given values.
     *
     * @param dx the x offset
     * @param dy the y offset
     * @param dz the z offset
     *
     * @return the offset block position
     */
    @NotNull
    public BlockPosition offset(int dx, int dy, int dz) {
        return new BlockPosition(x + dx, y + dy, z + dz);
    }

    /**
     * Get a new {@link BlockPosition} relative to the given {@link BlockFace}.
     *
     * @param face the direction in which to get the relative position
     *
     * @return the relative position
     */
    @NotNull
    public BlockPosition getRelative(@NotNull BlockFace face) {
        return face.getRelative(this);
    }

    /**
     * Get the distance squared between this block and the given set of coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return the distance squared in blocks
     *
     * @see #distance(int, int, int)
     */
    public double distanceSquared(int x, int y, int z) {
        return Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2) + Math.pow(z - this.z, 2);
    }

    /**
     * Get the distance between this block and the given set of coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return the distance in blocks
     *
     * @see #distanceSquared(int, int, int)
     */
    public double distance(int x, int y, int z) {
        return Math.sqrt(distanceSquared(x, y, z));
    }

    /**
     * Pack this {@link BlockPosition}'s coordinates into a primitive long.
     *
     * @return the packed position
     */
    public long pack() {
        return pack(x, y, z);
    }

    /**
     * Get a {@link BlockPosition} at the given coordinates.
     * <p>
     * Convenience method. Equivalent to invoking the constructor with the same arguments
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return the created {@link BlockPosition}
     */
    @NotNull
    public static BlockPosition at(int x, int y, int z) {
        return new BlockPosition(x, y, z);
    }

    /**
     * Unpack a {@link BlockPosition} from the given packed long.
     *
     * @param packedPos the packed long position
     *
     * @return the resulting BlockPosition
     */
    @NotNull
    public static BlockPosition unpack(long packedPos) {
        return new BlockPosition(unpackX(packedPos), unpackY(packedPos), unpackZ(packedPos));
    }

    /**
     * Packs the given coordinates into a primitive long.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return the packed position
     */
    public static long pack(int x, int y, int z) {
        return (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | ((long) y & 0xFFF);
    }

    /**
     * Unpack an x coordinate from the given packed long.
     *
     * @param packedPos the position as a long
     *
     * @return the unpacked x coordinate
     */
    public static int unpackX(long packedPos) {
        int value = (int) ((packedPos >> 38) & 0x3FFFFFF);

        if (value > MAXIMUM_UPPER_BOUND) {
            value -= NEGATIVE_UPPER_BOUND;
        }

        return value;
    }

    /**
     * Unpack a y coordinate from the given packed long.
     *
     * @param packedPos the position as a long
     *
     * @return the unpacked y coordinate
     */
    public static int unpackY(long packedPos) {
        int value = (int) (packedPos & 0xFFF);

        if (value > MAXIMUM_UPPER_BOUND_Y) {
            value -= NEGATIVE_UPPER_BOUND_Y;
        }

        return value;
    }

    /**
     * Unpack a z coordinate from the given packed long.
     *
     * @param packedPos the position as a long
     *
     * @return the unpacked z coordinate
     */
    public static int unpackZ(long packedPos) {
        int value = (int) ((packedPos >> 12) & 0x3FFFFFF);

        if (value > MAXIMUM_UPPER_BOUND) {
            value -= NEGATIVE_UPPER_BOUND;
        }

        return value;
    }

}
