package wtf.choco.veinminer.block;

import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;

/**
 * Represents a block understood by vein miner.
 */
public interface VeinMinerBlock extends Comparable<VeinMinerBlock> {

    /**
     * The wildcard block.
     *
     * @see VeinMinerBlockWildcard
     */
    public static final VeinMinerBlock WILDCARD = new VeinMinerBlockWildcard();

    /**
     * Check whether or not this {@link VeinMinerBlock} matches the given {@link Material}.
     *
     * @param type the type to check
     *
     * @return true if this block matches the given type, false otherwise
     */
    public boolean matchesType(@NotNull Material type);

    /**
     * Check whether or not this {@link VeinMinerBlock} matches the given {@link BlockData}.
     *
     * @param state the state to check
     * @param exact whether or not to match against all states
     *
     * @return true if matches, false otherwise
     */
    public boolean matchesState(@NotNull BlockData state, boolean exact);

    /**
     * Check whether or not this {@link VeinMinerBlock} matches the given {@link BlockData}.
     * <p>
     * The provided state will match if the states explicitly declared by this block
     * match. Any states not explicitly set by this block will be ignored.
     *
     * @param state the state to check
     *
     * @return true if matches, false otherwise
     */
    public default boolean matchesState(@NotNull BlockData state) {
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
     * minecraft:chest[facing=north,waterlogged=true]
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

        if (stated) {
            try {
                return new VeinMinerBlockState(Bukkit.createBlockData(matcher.group()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        else {
            Material type = Material.matchMaterial(matcher.group(1));
            return (type != null && type.isBlock()) ? new VeinMinerBlockType(type) : null;
        }
    }

}
