package wtf.choco.veinminer.tool;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.platform.world.ItemStack;
import wtf.choco.veinminer.platform.world.ItemType;

/**
 * A registry to which {@link VeinMinerToolCategory VeinMinerToolCategories} may be registered.
 */
public final class ToolCategoryRegistry {

    private static final Predicate<VeinMinerToolCategory> PREDICATE_ALWAYS_TRUE = category -> true;

    private final Map<String, VeinMinerToolCategory> categories = new HashMap<>();

    /**
     * Register the given {@link VeinMinerToolCategory}.
     *
     * @param category the category to register
     */
    public void register(@NotNull VeinMinerToolCategory category) {
        this.categories.put(category.getId().toLowerCase(), category);
    }

    /**
     * Get the {@link VeinMinerToolCategory} with the given id.
     *
     * @param id the id of the category to get
     *
     * @return the category, or null if none exists
     */
    @Nullable
    public VeinMinerToolCategory get(@NotNull String id) {
        return categories.get(id.toLowerCase());
    }

    @Nullable
    private VeinMinerToolCategory get(@NotNull ItemType itemType, @Nullable String itemNbtValue, @NotNull Predicate<VeinMinerToolCategory> categoryPredicate) {
        VeinMinerToolCategory resultCategory = null;
        for (VeinMinerToolCategory category : categories.values()) {
            if (category.containsItem(itemType) && (category.getNBTValue() == null || category.getNBTValue().equals(itemNbtValue)) && (resultCategory == null || category.compareTo(resultCategory) > 1) && categoryPredicate.test(category)) {
                resultCategory = category;
            }
        }

        return resultCategory;
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link ItemType}. There is
     * no guarantee as to which category will be returned if more than one category contains the
     * provided ItemType.
     *
     * @param itemType the item type
     *
     * @return the corresponding tool category, or null if no category contains the item
     *
     * @deprecated Using this leads to incorrect behaviour when an NBT tag is set
     * in config. Pass ItemStack instead of ItemType.
     */
    @Deprecated
    @Nullable
    public VeinMinerToolCategory get(@NotNull ItemType itemType) {
        return get(itemType, null, PREDICATE_ALWAYS_TRUE);
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link ItemType}. There is
     * no guarantee as to which category will be returned if more than one category contains the
     * provided ItemType.
     *
     * @param itemType the item type
     * @param categoryPredicate a predicate to apply on top of the item condition. If the predicate
     * returns false for any given category, it will not be returned by this method. Useful for an
     * additional permission check on a category
     *
     * @return the corresponding tool category, or null if no category contains the item
     *
     * @deprecated Using this leads to incorrect behaviour when an NBT tag is set
     * in config. Pass ItemStack instead of ItemType.
     */
    @Deprecated
    @Nullable
    public VeinMinerToolCategory get(@NotNull ItemType itemType, @NotNull Predicate<VeinMinerToolCategory> categoryPredicate) {
        return get(itemType, null, categoryPredicate);
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link ItemType}. There is
     * no guarantee as to which category will be returned if more than one category contains the
     * provided ItemType.
     *
     * @param itemStack the item
     *
     * @return the corresponding tool category, or null if no category contains the item
     */
    @Nullable
    public VeinMinerToolCategory get(@NotNull ItemStack itemStack) {
        return get(itemStack.getType(), itemStack.getVeinMinerNBTValue(), PREDICATE_ALWAYS_TRUE);
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link ItemType}. There is
     * no guarantee as to which category will be returned if more than one category contains the
     * provided ItemType.
     *
     * @param itemStack the item
     * @param categoryPredicate a predicate to apply on top of the item condition. If the predicate
     * returns false for any given category, it will not be returned by this method. Useful for an
     * additional permission check on a category
     *
     * @return the corresponding tool category, or null if no category contains the item
     */
    @Nullable
    public VeinMinerToolCategory get(@NotNull ItemStack itemStack, @NotNull Predicate<VeinMinerToolCategory> categoryPredicate) {
        return get(itemStack.getType(), itemStack.getVeinMinerNBTValue(), categoryPredicate);
    }

    /**
     * Unregister the given {@link VeinMinerToolCategory}.
     *
     * @param category the category to unregister
     *
     * @return true if unregistered, false if the category was not registered
     */
    public boolean unregister(@NotNull VeinMinerToolCategory category) {
        return (unregister(category.getId()) != null);
    }

    /**
     * Unregister the {@link VeinMinerToolCategory} with the given id.
     *
     * @param id the id of the category
     *
     * @return the category that was unregistered, or null if not registered
     */
    @Nullable
    public VeinMinerToolCategory unregister(@NotNull String id) {
        return categories.remove(id.toLowerCase());
    }

    /**
     * Get the amount of registered tool categories.
     *
     * @return size of this registry
     */
    public int size() {
        return categories.size();
    }

    /**
     * Get all registered {@link VeinMinerToolCategory VeinMinerToolCategories}.
     *
     * @return all categories
     */
    @NotNull
    @UnmodifiableView
    public Collection<? extends VeinMinerToolCategory> getAll() {
        return Collections.unmodifiableCollection(categories.values());
    }

    /**
     * Unregister all tool categories.
     */
    public void unregisterAll() {
        this.categories.clear();
    }

}
