package wtf.choco.veinminer.block;

import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;

/**
 * Represents a block understood by vein miner.
 */
public sealed interface VeinMinerBlock extends Comparable<VeinMinerBlock>
    permits VeinMinerBlockState, VeinMinerBlockTag, VeinMinerBlockType, VeinMinerBlockWildcard {

    /**
     * Checks whether or not this block is tangible and can be defined as a specific type. For
     * blocks where this is false, when vein mined, subsequent blocks must be matched against the
     * type of the origin in order to avoid unintentional aliasing with other types within this
     * block implementation.
     *
     * @return true if tangible, false otherwise
     */
    public boolean isTangible();

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
     * Get a {@link VeinMinerBlock} for a {@link BlockData}.
     *
     * @param state the block state
     *
     * @return the vein miner block
     */
    @NotNull
    public static VeinMinerBlock state(@NotNull BlockData state) {
        return new VeinMinerBlockState(state);
    }

    /**
     * Get a {@link VeinMinerBlock} for a {@link Tag}.
     *
     * @param tag the block tag
     *
     * @return the vein miner block
     */
    @NotNull
    public static VeinMinerBlock tag(@NotNull Tag<Material> tag) {
        return new VeinMinerBlockTag(tag);
    }

    /**
     * Get a {@link VeinMinerBlock} for a {@link Material}.
     *
     * @param material the block type
     *
     * @return the vein miner block
     */
    @NotNull
    public static VeinMinerBlock type(@NotNull Material material) {
        return new VeinMinerBlockType(material);
    }

    /**
     * Get a wildcard {@link VeinMinerBlock}.
     *
     * @return the vein miner wildcard block
     */
    @NotNull
    public static VeinMinerBlock wildcard() {
        return VeinMinerBlockWildcard.INSTANCE;
    }

    /**
     * Get a {@link VeinMinerBlock} from a string. Example states:
     * <pre>
     * chest
     * minecraft:chest
     * minecraft:chest[waterlogged=true]
     * minecraft:chest[facing=north,waterlogged=true]
     * * // The wildcard state
     * </pre>
     * This method will also support a tag key such as {@code #minecraft:leaves}.
     *
     * @param string the string from which to parse a VeinMinerBlock instance
     *
     * @return the constructed VeinMinerBlock, or null if an invalid format was provided
     */
    @Nullable
    public static VeinMinerBlock fromString(@NotNull String string) {
        if (string.equals("*")) {
            return wildcard();
        }

        if (string.startsWith("#")) {
            NamespacedKey tagKey = NamespacedKey.fromString(string.substring(1));
            if (tagKey == null) {
                return null;
            }

            Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class); // TODO: Use the new tag system
            return tag != null ? tag(tag) : null;
        }

        Matcher matcher = VeinMiner.PATTERN_BLOCK_STATE.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        boolean stated = (matcher.group(2) != null);

        if (stated) {
            try {
                return state(Bukkit.createBlockData(matcher.group()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        else {
            Material type = Material.matchMaterial(matcher.group(1));
            return (type != null && type.isBlock()) ? type(type) : null;
        }
    }

}
