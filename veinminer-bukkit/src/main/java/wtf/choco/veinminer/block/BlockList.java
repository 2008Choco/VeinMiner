package wtf.choco.veinminer.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A list of {@link VeinMinerBlock VeinMinerBlocks}.
 */
public class BlockList implements Iterable<VeinMinerBlock>, Cloneable {

    private final Set<VeinMinerBlock> blocks;

    /**
     * Construct a new {@link BlockList} containing the values of the given lists.
     * Duplicate entries will be ignored.
     *
     * @param lists the block lists
     */
    public BlockList(@NotNull BlockList... lists) {
        this.blocks = new HashSet<>();

        for (BlockList list : lists) {
            this.addAll(list);
        }
    }

    /**
     * Construct a new {@link BlockList} containing the values of the given list.
     *
     * @param list the list
     */
    public BlockList(@NotNull BlockList list) {
        this.blocks = new HashSet<>(list.blocks);
    }

    /**
     * Construct a new {@link BlockList} with the given initial size.
     *
     * @param initialSize the initial size. Must be positive and non-zero
     */
    public BlockList(int initialSize) {
        this.blocks = new HashSet<>(initialSize);
    }

    /**
     * Construct a new empty {@link BlockList}.
     */
    public BlockList() {
        this.blocks = new HashSet<>();
    }

    /**
     * Add a {@link VeinMinerBlock} to this list.
     *
     * @param block the block to add
     *
     * @return true if this list was changed as a result of this operation, false if
     * remained unchanged
     */
    public boolean add(@NotNull VeinMinerBlock block) {
        return blocks.add(block);
    }

    /**
     * Add a collection of {@link VeinMinerBlock VeinMinerBlocks} to this list.
     *
     * @param blocks the blocks to add
     *
     * @return true if this list was changed as a result of this operation, false if
     * remained unchanged
     */
    public boolean addAll(@NotNull Iterable<? extends VeinMinerBlock> blocks) {
        boolean changed = false;

        for (VeinMinerBlock block : blocks) {
            changed |= this.blocks.add(block);
        }

        return changed;
    }

    /**
     * Remove a {@link VeinMinerBlock} from this list.
     *
     * @param block the block to remove
     *
     * @return true if this list contained the given block
     */
    public boolean remove(@NotNull VeinMinerBlock block) {
        return blocks.remove(block);
    }

    /**
     * Remove from this list the block that matches the given state.
     *
     * @param state the state to remove
     *
     * @return true if this list contained a block with the given state
     */
    public boolean remove(@NotNull BlockData state) {
        return blocks.removeIf(block -> block.matchesState(state, true));
    }

    /**
     * Remove from this list all blocks that match the given type regardless of their
     * states.
     *
     * @param type the type of block to remove
     *
     * @return true if this list contained at least one block with the given type
     */
    public boolean removeAll(@NotNull Material type) {
        return blocks.removeIf(block -> block.matchesType(type));
    }

    /**
     * Check whether or not this list contains the given block.
     *
     * @param block the block to check
     *
     * @return true if this list contains the value, false otherwise
     */
    public boolean contains(@NotNull VeinMinerBlock block) {
        return blocks.contains(block);
    }

    /**
     * Check whether or not this list contains a block that matches the given state.
     *
     * @param state the state to check
     * @param exact whether or not to match against all states
     *
     * @return true if this list contains the state, false otherwise
     */
    public boolean containsState(@NotNull BlockData state, boolean exact) {
        return containsOnPredicate(block -> block.matchesState(state, exact));
    }

    /**
     * Check whether or not this list contains a block that matches the given state.
     *
     * @param state the state to check
     *
     * @return true if this list contains the state, false otherwise
     */
    public boolean containsState(@NotNull BlockData state) {
        return containsOnPredicate(block -> block.matchesState(state, false));
    }

    /**
     * Check whether or not this list contains a block that matches the given type.
     *
     * @param type the type to check
     *
     * @return true if this list contains the type, false otherwise
     */
    public boolean containsType(@NotNull Material type) {
        return containsOnPredicate(block -> block.matchesType(type));
    }

    /**
     * Check whether or not this list contains a wildcard.
     *
     * @return true if contains wildcard, false otherwise
     */
    public boolean containsWildcard() {
        return containsOnPredicate(block -> block == VeinMinerBlock.wildcard());
    }

    private boolean containsOnPredicate(@NotNull Predicate<VeinMinerBlock> predicate) {
        for (VeinMinerBlock block : blocks) {
            if (predicate.test(block)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the {@link VeinMinerBlock} from this {@link BlockList} that matches the given
     * {@link BlockData}. If no VeinMinerBlock in this list matches the BlockState (i.e.
     * {@link VeinMinerBlock#matchesState(BlockData)} is {@code false}), null is returned.
     *
     * @param state the state for which to get a VeinMinerBlock
     *
     * @return the matching VeinBlock for this list, or null if none
     */
    @Nullable
    public VeinMinerBlock getVeinMinerBlock(@NotNull BlockData state) {
        for (VeinMinerBlock block : blocks) {
            if (block.matchesState(state)) {
                return block;
            }
        }

        return null;
    }

    /**
     * Get the size of this list.
     *
     * @return the size
     */
    public int size() {
        return blocks.size();
    }

    /**
     * Check whether or not this {@link BlockList} is empty.
     *
     * @return true if empty (size is 0), false if it contains at least one entry
     */
    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    /**
     * Clear this block list.
     */
    public void clear() {
        this.blocks.clear();
    }

    /**
     * Get this {@link BlockList} as a {@link Set}.
     *
     * @return a new set matching the elements of this list
     */
    @NotNull
    public Set<VeinMinerBlock> asSet() {
        return new HashSet<>(blocks);
    }

    /**
     * Get this {@link BlockList} as a sorted {@link List}.
     *
     * @param comparator the comparator by which to sort the elements, or null to use
     * {@link VeinMinerBlock}'s natural sorting order
     *
     * @return a new list matching the elements of this list
     */
    @NotNull
    public List<VeinMinerBlock> asList(@Nullable Comparator<VeinMinerBlock> comparator) {
        List<VeinMinerBlock> blocks = new ArrayList<>(this.blocks);
        blocks.sort(comparator);
        return blocks;
    }

    /**
     * Get this {@link BlockList} as a {@link List}.
     *
     * @return a new list matching the elements of this list
     */
    @NotNull
    public List<VeinMinerBlock> asList() {
        return new ArrayList<>(blocks);
    }

    /**
     * Create a {@link BlockList} from a list of input strings.
     *
     * @param blockStateStrings the list of strings from which to create a BlockList
     * @param logger an optional logger instance to which warnings should be logged
     *
     * @return the block list
     */
    @NotNull
    public static BlockList parseBlockList(@NotNull Collection<String> blockStateStrings, @Nullable Logger logger) {
        BlockList blocklist = new BlockList();

        for (String blockStateString : blockStateStrings) {
            VeinMinerBlock veinMinerBlock = VeinMinerBlock.fromString(blockStateString);
            if (veinMinerBlock == null) {
                if (logger != null) {
                    logger.warning(String.format("Unknown or invalid block state string for input: \"%s\". Is it an item?", blockStateString));
                }

                continue;
            }

            if (!blocklist.add(veinMinerBlock) && logger != null) {
                logger.warning(String.format("Duplicate block state string: \"%s\"", blockStateString));
            }
        }

        return blocklist;
    }

    @NotNull
    @Override
    public Iterator<VeinMinerBlock> iterator() {
        return blocks.iterator();
    }

    @NotNull
    @Override
    public BlockList clone() {
        return new BlockList(this);
    }

    @Override
    public int hashCode() {
        return blocks.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BlockList other && Objects.equals(blocks, other.blocks));
    }

    @Override
    public String toString() {
        StringJoiner stateJoiner = new StringJoiner(", ", "[", "]");
        this.blocks.forEach(block -> stateJoiner.add(block.toStateString()));
        return getClass().getSimpleName() + stateJoiner.toString();
    }

}
