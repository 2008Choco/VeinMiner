package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a block with explicitly set states.
 */
public interface BlockState {

    /**
     * Get the {@link BlockType} represented by this {@link BlockState}.
     *
     * @return the block type
     */
    @NotNull
    public BlockType getType();

    /**
     * Get this {@link BlockState} as a string.
     *
     * @param hideUnspecified whether or not to hide states that were not explicitly set
     * in the construction of this state
     *
     * @return the string
     */
    @NotNull
    public String getAsString(boolean hideUnspecified);

    /**
     * Check whether or not this {@link BlockState} matches the given BlockState.
     * <p>
     * A BlockState will match if all states that were explicitly set by this BlockState match
     * that of the provided BlockState. Any states that were not explicitly set are ignored.
     * Therefore, while {@code a.matches(b)} may return {@code true}, it cannot be guaranteed
     * that {@code b.matches(a)} will also return {@code true}.
     *
     * @param state the state to check
     *
     * @return true if the provided BlockState matches this BlockState, false if one of its states
     * do not match one of this BlockState's explicitly set states
     */
    public boolean matches(@NotNull BlockState state);

}
