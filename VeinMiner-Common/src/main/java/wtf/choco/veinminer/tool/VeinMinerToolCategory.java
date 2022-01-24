package wtf.choco.veinminer.tool;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.platform.ItemType;

/**
 * Represents a category of tools.
 */
public interface VeinMinerToolCategory {

    /**
     * Get the unique id of this tool category.
     *
     * @return the category id
     */
    @NotNull
    public String getId();

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
     */
    public boolean containsItem(@NotNull ItemType item);

}
