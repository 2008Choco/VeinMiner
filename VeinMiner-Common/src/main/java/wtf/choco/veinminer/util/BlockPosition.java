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

}
