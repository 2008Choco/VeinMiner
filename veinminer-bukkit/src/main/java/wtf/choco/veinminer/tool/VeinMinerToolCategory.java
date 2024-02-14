package wtf.choco.veinminer.tool;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.util.ItemStackUtil;
import wtf.choco.veinminer.util.VMConstants;

/**
 * Represents a category of tools.
 */
public class VeinMinerToolCategory implements Comparable<VeinMinerToolCategory> {

    private final String id;
    private final int priority;
    private final String nbtValue;
    private final BlockList blockList;
    private final ToolCategoryConfiguration config;
    private final Set<Material> items;

    /**
     * Construct a new {@link VeinMinerToolCategory}.
     *
     * @param id the unique id of the tool category
     * @param priority the category's priority
     * @param nbtValue the required value of the NBT key
     * @param blockList the category block list
     * @param config the category config
     * @param items the items in this category
     */
    public VeinMinerToolCategory(@NotNull String id, int priority, @Nullable String nbtValue, @NotNull BlockList blockList, @NotNull ToolCategoryConfiguration config, @NotNull Set<Material> items) {
        this.id = id;
        this.priority = priority;
        this.nbtValue = nbtValue;
        this.blockList = blockList;
        this.config = config;
        this.items = new HashSet<>(items);
    }

    /**
     * Get the unique id of this tool category.
     *
     * @return the category id
     */
    @NotNull
    public String getId() {
        return id;
    }

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
    public int getPriority() {
        return priority;
    }

    /**
     * Get the NBT value that must be on items in this category to be vein mineable.
     * <p>
     * Callers of {@link #containsItem(Material)} should verify also that any ItemStack to be
     * checked contains also the value returned by this method.
     *
     * @return the NBT value, or null if no NBT is required
     *
     * @see VMConstants#getVeinMinerNBTKey()
     */
    @Nullable
    public String getNBTValue() {
        return nbtValue;
    }

    /**
     * Get the {@link BlockList} for this tool category.
     *
     * @return the block list
     */
    @NotNull
    public BlockList getBlockList() {
        return blockList;
    }

    /**
     * Get the {@link ToolCategoryConfiguration} for this tool category.
     * <p>
     * The returned configuration may be edited. It will be saved in memory until saved via the
     * {@link VeinMinerPlugin#getCategoriesConfig()}.
     *
     * @return the config
     */
    @NotNull
    public ToolCategoryConfiguration getConfiguration() {
        return config;
    }

    /**
     * Add an item to this category.
     *
     * @param item the item type to add
     *
     * @return true if the item list was modified, false if the item was already added
     */
    public boolean addItem(@NotNull Material item) {
        return items.add(item);
    }

    /**
     * Remove an item from this category.
     *
     * @param material the item type to remove
     *
     * @return true if the item list was modified, false if the item had not been added
     */
    public boolean removeItem(@NotNull Material material) {
        return items.remove(material);
    }

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
    public boolean containsItem(@NotNull Material item) {
        return items.contains(item);
    }

    /**
     * Get an unmodifiable {@link Set} of all items in this category.
     *
     * @return the items
     */
    @NotNull
    @UnmodifiableView
    public Set<Material> getItems() {
        return Collections.unmodifiableSet(items);
    }

    /**
     * Create an {@link ItemStack} with the given {@link Material} that would be a valid item
     * for this category (including its NBT value if present).
     *
     * @param material the item type to create. Must be in this category's item list
     * @param amount the item quantity
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack createItemStack(@NotNull Material material, int amount) {
        Preconditions.checkArgument(material != null, "material must not be null");
        Preconditions.checkArgument(items.contains(material), "material must be an item in this category's item list");
        Preconditions.checkArgument(amount >= 1, "amount must be >= 1");

        ItemStack itemStack = new ItemStack(material, amount);
        if (nbtValue != null) {
            ItemStackUtil.setVeinMinerNBTValue(itemStack, nbtValue);
        }

        return itemStack;
    }

    /**
     * Create an {@link ItemStack} with the given {@link Material} that would be a valid item
     * for this category (including its NBT value if present).
     *
     * @param material the item type to create. Must be in this category's item list
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack createItemStack(@NotNull Material material) {
        return createItemStack(material, 1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default behaviour is to compare against a category's priority ({@link #getPriority()}).
     */
    @Override
    public int compareTo(VeinMinerToolCategory other) {
        return Integer.compare(priority, other.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockList, priority, config, id, items);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof VeinMinerToolCategory other)) {
            return false;
        }

        return Objects.equals(id, other.id)
                && priority == other.priority
                && Objects.equals(blockList, other.blockList)
                && Objects.equals(config, other.config)
                && Objects.equals(items, other.items);
    }

    @Override
    public String toString() {
        StringJoiner itemJoiner = new StringJoiner(", ", "\"", "\"");
        this.items.forEach(item -> itemJoiner.add(item.getKey().toString()));

        return String.format("VeinMinerToolCategory[id=\"%s\", priority=%d, blockList=\"%s\", config=\"%s\", items=%s]", id, priority, blockList, config, itemJoiner.toString());
    }

}
