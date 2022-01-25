package wtf.choco.veinminer.block;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.BlockState;
import wtf.choco.veinminer.platform.BlockType;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * A generic interface to get types and states from coordinates.
 */
public interface BlockAccessor {

    /**
     * Get the name of the world being accessed by this {@link BlockAccessor}.
     *
     * @return the world name
     */
    @NotNull
    public String getWorldName();

    /**
     * Get the {@link BlockType} at the given coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return the type
     */
    @NotNull
    public BlockType getType(int x, int y, int z);

    /**
     * Get the {@link BlockType} at the given {@link BlockPosition}.
     *
     * @param position the position
     *
     * @return the type
     */
    @NotNull
    public default BlockType getType(@NotNull BlockPosition position) {
        return getType(position.x(), position.y(), position.z());
    }

    /**
     * Get the {@link BlockState} at the given coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return the state
     */
    @NotNull
    public BlockState getState(int x, int y, int z);

    /**
     * Get the {@link BlockState} at the given {@link BlockPosition}.
     *
     * @param position the position
     *
     * @return the state
     */
    @NotNull
    public default BlockState getState(@NotNull BlockPosition position) {
        return getState(position.x(), position.y(), position.z());
    }

}
