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
        return ((long) x & 0x7FFFFFF) | (((long) z & 0x7FFFFFF) << 27) | ((long) y << 54);
    }

    /**
     * Unpack an x coordinate from the given packed long.
     *
     * @param packedPos the position as a long
     *
     * @return the unpacked x coordinate
     */
    public static int unpackX(long packedPos) {
        return (int) ((packedPos << 37) >> 37);
    }

    /**
     * Unpack a y coordinate from the given packed long.
     *
     * @param packedPos the position as a long
     *
     * @return the unpacked y coordinate
     */
    public static int unpackY(long packedPos) {
        return (int) (packedPos >> 54);
    }

    /**
     * Unpack a z coordinate from the given packed long.
     *
     * @param packedPos the position as a long
     *
     * @return the unpacked z coordinate
     */
    public static int unpackZ(long packedPos) {
        return (int) ((packedPos << 10) >> 37);
    }

}
