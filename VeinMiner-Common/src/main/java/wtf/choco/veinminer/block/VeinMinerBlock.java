package wtf.choco.veinminer.block;

import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.platform.BlockState;
import wtf.choco.veinminer.platform.BlockType;
import wtf.choco.veinminer.platform.PlatformReconstructor;

/**
 * Represents a block understood by VeinMiner.
 */
public interface VeinMinerBlock extends Comparable<VeinMinerBlock> {

    /**
     * The wildcard block.
     *
     * @see VeinMinerBlockWildcard
     */
    public static final VeinMinerBlock WILDCARD = new VeinMinerBlockWildcard();

    /**
     * Get the type represented by this block.
     *
     * @return the type
     */
    @NotNull
    public BlockType getType();

    /**
     * Get the block state represented by this block.
     * <p>
     * If this block has no state (e.g. {@link #hasState()} is {@code false}), this method
     * should return a state with default block state values.
     *
     * @return the state
     */
    @NotNull
    public BlockState getState();

    /**
     * Check whether or not this block has at least one state set.
     *
     * @return true if a state is set, false otherwise
     */
    public boolean hasState();

    /**
     * Check whether or not this {@link VeinMinerBlock} matches the given type.
     *
     * @param type the type to check
     *
     * @return true if this block matches the given type, false otherwise
     */
    public boolean matchesType(@NotNull BlockType type);

    /**
     * Check whether or not this {@link VeinMinerBlock} matches the given state.
     *
     * @param state the state to check
     * @param exact whether or not to match against all states
     *
     * @return true if matches, false otherwise
     */
    public boolean matchesState(@NotNull BlockState state, boolean exact);

    /**
     * Check whether or not this {@link VeinMinerBlock} matches the given state.
     * <p>
     * The provided state will match if the states explicitly declared by this block
     * match. Any states not explicitly set by this block will be ignored.
     *
     * @param state the state to check
     *
     * @return true if matches, false otherwise
     */
    public default boolean matchesState(@NotNull BlockState state) {
        return matchesState(state, false);
    }

    /**
     * Get this {@link VeinMinerBlock} as a state string.
     *
     * @return the state string
     */
    @NotNull
    public String toStateString();

    @Override
    public default int compareTo(@Nullable VeinMinerBlock other) {
        return (other != null) ? toStateString().compareTo(other.toStateString()) : 1;
    }

    /**
     * Get a {@link VeinMinerBlock} from a string.
     *
     * Example states:
     * <pre>
     * chest
     * minecraft:chest
     * minecraft:chest[waterlogged=true]
     * minecraft:chest[facing=north, waterlogged=true]
     * * // The wildcard state
     * </pre>
     *
     * @param string the string from which to parse a VeinMinerBlock instance
     *
     * @return the constructed VeinMinerBlock, or null if an invalid format was provided
     */
    @Nullable
    public static VeinMinerBlock fromString(@NotNull String string) {
        if (string.equals("*")) {
            return WILDCARD;
        }

        Matcher matcher = VeinMiner.PATTERN_BLOCK_STATE.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        boolean stated = (matcher.group(2) != null);
        PlatformReconstructor reconstructor = VeinMiner.getInstance().getPlatformReconstructor();

        if (stated) {
            BlockState state = reconstructor.getState(matcher.group());
            return (state != null) ? new VeinMinerBlockState(state) : null;
        }
        else {
            BlockType type = reconstructor.getBlockType(matcher.group(1));
            return (type != null) ? new VeinMinerBlockType(type) : null;
        }
    }

}
