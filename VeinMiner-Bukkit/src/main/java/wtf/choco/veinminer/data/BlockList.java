package wtf.choco.veinminer.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.block.VeinBlock;

/**
 * Represents a list of blocks and states (see {@link VeinBlock}).
 *
 * @author Parker Hawke - 2008Choco
 */
public class BlockList implements Iterable<VeinBlock>, Serializable, Cloneable {

    private static final long serialVersionUID = -2274615459168041997L;

    private final Set<VeinBlock> blocks;

    /**
     * Create a new BlockList with the values from a set of existing {@link BlockList} instances.
     * Duplicate instances of blocks and states will not be included.
     *
     * @param lists the block lists whose values should be included
     */
    public BlockList(@NotNull BlockList... lists) {
        int expectedSize = 0; // Obviously doesn't take into consideration duplicates
        for (BlockList list : lists) {
            expectedSize += list.size();
        }

        this.blocks = new HashSet<>(expectedSize);
        for (BlockList list : lists) {
            this.addAll(list);
        }
    }

    /**
     * Create a new BlockList with the values from an existing {@link BlockList} instance.
     *
     * @param list the block list whose values should be included
     */
    public BlockList(@NotNull BlockList list) {
        this(list.size());
        this.addAll(list);
    }

    /**
     * Create a new BlockList with an initial size
     *
     * @param initialSize the initial list size
     */
    public BlockList(int initialSize) {
        this.blocks = new HashSet<>(initialSize);
    }

    /**
     * Create a new, empty Blocklist with its default initial capacity (16)
     */
    public BlockList() {
        this.blocks = new HashSet<>();
    }

    /**
     * Add a {@link BlockData} to this BlockList.
     *
     * @param data the data to add
     *
     * @return the VeinBlock added to this list
     */
    @NotNull
    public VeinBlock add(@NotNull BlockData data) {
        VeinBlock block = VeinBlock.get(data);
        this.add(block);
        return block;
    }

    /**
     * Add a {@link Material} with no states to this BlockList.
     *
     * @param material the material to add
     *
     * @return the VeinBlock added to this list
     */
    @NotNull
    public VeinBlock add(@NotNull Material material) {
        // Remove any specific data with this type before adding a wildcard
        Iterator<VeinBlock> iterator = iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getType() == material) {
                iterator.remove();
            }
        }

        VeinBlock block = VeinBlock.get(material);
        this.add(block);
        return block;
    }

    /**
     * Add a VeinBlock to this block list. If the block is already present in this list, this
     * method will fail silently.
     *
     * @param block the block to add
     *
     * @return true if added, false if already present
     */
    public boolean add(@NotNull VeinBlock block) {
        return blocks.add(block);
    }

    /**
     * Add a collection of VeinBlocks to this block list. If a block is already present in this list,
     * it will be ignored silently.
     *
     * @param values the values to add
     *
     * @return true if at least one block was added, false otherwise
     */
    public boolean addAll(@NotNull Iterable<? extends VeinBlock> values) {
        boolean changed = false;

        for (VeinBlock block : values) {
            changed |= blocks.add(block);
        }

        return changed;
    }

    /**
     * Remove a specific VeinBlock from this block list.
     *
     * @param block the block to remove
     */
    public void remove(@NotNull VeinBlock block) {
        this.blocks.remove(block);
    }

    /**
     * Remove a specific BlockData from this block list. The data must be {@link #equals(Object)} to
     * the one that should be removed.
     *
     * @param data the data to remove
     */
    public void remove(@NotNull BlockData data) {
        this.blocks.removeIf(block -> block.getBlockData().equals(data));
    }

    /**
     * Remove all blocks and states from this list such that their type match the one specified.
     *
     * @param material the material to remove
     */
    public void removeAll(@NotNull Material material) {
        this.blocks.removeIf(block -> block.getType() == material);
    }

    /**
     * Check whether or not this list contains a specific {@link VeinBlock} instance.
     *
     * @param block the block to check
     *
     * @return true if present, false otherwise
     */
    public boolean contains(@NotNull VeinBlock block) {
        return blocks.contains(block);
    }

    /**
     * Check whether or not this list encapsulates the specified {@link BlockData}. This method
     * will return true if even a parent VeinBlock (i.e. one with just a similar material or more
     * generic states) is present, therefore if an exact check is required, see
     * {@link #containsExact(BlockData)}.
     *
     * @param data the data to check
     *
     * @return true if present, false otherwise
     *
     * @see #containsExact(BlockData)
     */
    public boolean contains(@NotNull BlockData data) {
        return containsOnPredicate(block -> block.encapsulates(data));
    }

    /**
     * Check whether or not this list encapsulates the specified {@link Material}.
     *
     * @param material the material to check
     *
     * @return true if present, false otherwise
     */
    public boolean contains(@NotNull Material material) {
        return containsOnPredicate(block -> block.getType() == material);
    }

    /**
     * Check whether or not this list contains exactly the specified {@link BlockData}.
     *
     * @param data the data to check
     *
     * @return true if present, false otherwise
     */
    public boolean containsExact(@NotNull BlockData data) {
        return containsOnPredicate(block -> block.getBlockData().equals(data));
    }

    /**
     * Check whether or not this blocklist contains a wildcard.
     *
     * @return true if wildcarded, false otherwise
     */
    public boolean containsWildcard() {
        return containsOnPredicate(VeinBlock::isWildcard);
    }

    /**
     * Get the VeinBlock from this {@link BlockList} that encapsulates the given {@link BlockData}.
     * If no VeinBlock in this list encapsulates the BlockData, (i.e. {@link #contains(BlockData)}
     * is false) null is returned.
     *
     * @param data the data for which to get a VeinBlock
     *
     * @return the encapsulating VeinBlock for this list. null if none
     */
    @Nullable
    public VeinBlock getVeinBlock(@NotNull BlockData data) {
        for (VeinBlock block : blocks) {
            if (block.encapsulates(data)) {
                return block;
            }
        }

        return null;
    }

    private boolean containsOnPredicate(@NotNull Predicate<VeinBlock> predicate) {
        for (VeinBlock block : blocks) {
            if (predicate.test(block)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the size of this list.
     *
     * @return the list size
     */
    public int size() {
        return blocks.size();
    }

    /**
     * Clear the contents of this list.
     */
    public void clear() {
        this.blocks.clear();
    }

    @Override
    @NotNull
    public Iterator<VeinBlock> iterator() {
        return blocks.iterator();
    }

    @Override
    @NotNull
    public BlockList clone() {
        return new BlockList(this);
    }

    @Override
    public int hashCode() {
        return blocks.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BlockList && Objects.equals(blocks, ((BlockList) obj).blocks));
    }

}
