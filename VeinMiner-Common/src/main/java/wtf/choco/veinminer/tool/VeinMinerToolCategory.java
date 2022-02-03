package wtf.choco.veinminer.tool;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.platform.ItemType;

/**
 * Represents a category of tools.
 */
public interface VeinMinerToolCategory extends Comparable<VeinMinerToolCategory> {

    /**
     * Get the unique id of this tool category.
     *
     * @return the category id
     */
    @NotNull
    public String getId();

    /**
     * Get the priority of this tool category.
     * <p>
     * Priority determines whether or not one category will be selected over another under
     * the circumstance that more than one category matches any given item type. For instance,
     * if two categories declare that a {@code diamond_pickaxe} is on the list of items, the
     * category with the higher priority should be selected for use. Higher integers are
     * more important than categories with lower integers. Categories with the same priority
     * will not abide by any defined behaviour.
     *
     * @return the priority
     */
    public int getPriority();

    /**
     * Get the value that must be on items in this category to be vein mineable.
     * <p>
     * Callers of {@link #containsItem(ItemType)} should verify also that any ItemStack to be
     * checked contains also the value returned by this method.
     *
     * @return the NBT value, or null if no NBT is required
     */
    @Nullable
    public String getNBTValue();

    /**
     * Get the {@link BlockList} for this tool category.
     *
     * @return the block list
     */
    @NotNull
    public BlockList getBlockList();

    /**
     * Get the {@link VeinMinerConfig} for this tool category.
     *
     * @return the config
     */
    @NotNull
    public VeinMinerConfig getConfig();

    /**
     * Add an item to this category.
     *
     * @param itemType the item type to add
     *
     * @return true if the item list was modified, false if the item was already added
     */
    public boolean addItem(@NotNull ItemType itemType);

    /**
     * Remove an item from this category.
     *
     * @param itemType the item type to remove
     *
     * @return true if the item list was modified, false if the item had not been added
     */
    public boolean removeItem(@NotNull ItemType itemType);

    /**
     * Get an unmodifiable collection of all items in this category.
     *
     * @return the items
     */
    @NotNull
    @UnmodifiableView
    public Set<ItemType> getItems();

    /**
     * Check whether or not this category contains the given item.
     *
     * @param item the item to check
     *
     * @return true if this category contains the item, false otherwise
     *
     * @apiNote this method only verifies that the type is contained in this category, not whether
     * or not an item stack contains the required NBT. For this, callers should also check that
     * the value of {@link #getNBTValue()} is present in addition to the result of this method.
     */
    public boolean containsItem(@NotNull ItemType item);

    /**
     * {@inheritDoc}
     * <p>
     * Default behaviour is to compare against a category's priority ({@link #getPriority()}).
     */
    @Override
    default int compareTo(VeinMinerToolCategory other) {
        return Integer.compare(getPriority(), other.getPriority());
    }

}
