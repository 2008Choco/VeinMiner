package wtf.choco.veinminer.tool;

import com.google.common.base.Predicates;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.util.ItemStackUtil;

/**
 * A registry to which {@link VeinMinerToolCategory VeinMinerToolCategories} may be registered.
 */
public final class ToolCategoryRegistry {

    private final Map<String, VeinMinerToolCategory> categories = new HashMap<>();

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link ToolCategoryRegistry}.
     *
     * @param plugin the plugin instance
     */
    public ToolCategoryRegistry(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

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
    private VeinMinerToolCategory get(@NotNull Material item, @Nullable String itemNbtValue, @NotNull Predicate<VeinMinerToolCategory> categoryPredicate) {
        VeinMinerToolCategory resultCategory = null;
        for (VeinMinerToolCategory category : categories.values()) {
            if (!category.containsItem(item)) {
                continue;
            }

            String nbtValue = category.getNBTValue();
            if (!Objects.equals(nbtValue, itemNbtValue)) {
                continue;
            }

            // If the category's priority is lower than the currently returnable category, ignore it
            if (resultCategory != null && category.compareTo(category) <= 0) {
                continue;
            }

            if (categoryPredicate.test(category)) {
                resultCategory = category;
            }
        }

        return resultCategory;
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link Material}. There is
     * no guarantee which category will be returned if more than one category contains the provided
     * item type.
     *
     * @param item the item type
     *
     * @return the corresponding tool category, or null if no category contains the item
     *
     * @deprecated Using this leads to incorrect behaviour when an NBT tag is set
     * in config. Pass ItemStack instead of ItemType.
     */
    @Deprecated
    @Nullable
    public VeinMinerToolCategory get(@NotNull Material item) {
        return get(item, null, Predicates.alwaysTrue());
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link Material}. There is
     * no guarantee which category will be returned if more than one category contains the provided
     * item type.
     *
     * @param item the item type
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
    public VeinMinerToolCategory get(@NotNull Material item, @NotNull Predicate<VeinMinerToolCategory> categoryPredicate) {
        return get(item, null, categoryPredicate);
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link Material}. There is
     * no guarantee which category will be returned if more than one category contains the provided
     * item type.
     *
     * @param itemStack the item
     *
     * @return the corresponding tool category, or null if no category contains the item
     */
    @Nullable
    public VeinMinerToolCategory get(@NotNull ItemStack itemStack) {
        return get(itemStack.getType(), ItemStackUtil.getVeinMinerNBTValue(itemStack), Predicates.alwaysTrue());
    }

    /**
     * Get the {@link VeinMinerToolCategory} that contains the given {@link Material}. There is
     * no guarantee which category will be returned if more than one category contains the provided
     * item type.
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
        return get(itemStack.getType(), ItemStackUtil.getVeinMinerNBTValue(itemStack), categoryPredicate);
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
     * Unregister all tool categories from this registry and re-register categories that have
     * been read and parsed from VeinMiner's configuration files.
     */
    public void reloadFromConfig() {
        this.unregisterAll();

        VeinMinerConfiguration config = plugin.getConfiguration();
        for (String categoryId : config.getDefinedCategoryIds()) {
            if (categoryId.contains(" ")) {
                this.plugin.getLogger().warning(String.format("Category id \"%s\" is invalid. Must not contain spaces (' ')", categoryId));
                continue;
            }

            ToolCategoryConfiguration categoryConfig = config.getToolCategoryConfiguration(categoryId);
            boolean hand = (categoryId.equalsIgnoreCase("Hand"));

            Set<Material> items = new HashSet<>();
            for (String itemTypeString : categoryConfig.getItemKeys()) {
                Material material = Material.matchMaterial(itemTypeString);

                if (material == null || !material.isItem()) {
                    this.plugin.getLogger().warning(String.format("Unknown item for input \"%s\". Did you spell it correctly?", itemTypeString));
                    continue;
                }

                items.add(material);
            }

            if (!hand && items.isEmpty()) {
                this.plugin.getLogger().warning(String.format("Category with id \"%s\" has no items. Ignoring registration.", categoryId));
                continue;
            }

            Collection<String> blockStateStrings = categoryConfig.getBlockListKeys();
            BlockList blocklist = BlockList.parseBlockList(blockStateStrings, plugin.getLogger());
            if (blocklist.size() == 0) {
                this.plugin.getLogger().warning(String.format("No block list configured for category with id \"%s\"! Is this intentional?", categoryId));
            }

            if (!hand) {
                int priority = categoryConfig.getPriority();
                String nbtValue = categoryConfig.getNBTValue();

                this.register(new VeinMinerToolCategory(categoryId, priority, nbtValue, blocklist, categoryConfig, items));
            } else {
                this.register(new VeinMinerToolCategoryHand(blocklist, categoryConfig));
            }

            this.plugin.getLogger().info(String.format("Registered category with id \"%s\" holding %d unique items and %d unique blocks.", categoryId, items.size(), blocklist.size()));
        }

        // Register permissions dynamically
        Permission veinminePermissionParent = getOrRegisterPermission("veinminer.veinmine.*", () -> "Allow the use of vein miner for all tool categories", PermissionDefault.TRUE);
        Permission blocklistPermissionParent = getOrRegisterPermission("veinminer.blocklist.list.*", () -> "Allow access to list the blocks in all block lists", PermissionDefault.OP);
        Permission toollistPermissionParent = getOrRegisterPermission("veinminer.toollist.list.*", () -> "Allow access to list the tools in a category's tool list", PermissionDefault.OP);

        for (VeinMinerToolCategory category : getAll()) {
            String id = category.getId().toLowerCase();

            Permission veinminePermission = getOrRegisterPermission("veinminer.veinmine." + id, () -> "Allows players to vein mine using the " + category.getId() + " category", PermissionDefault.TRUE);
            Permission blocklistPermission = getOrRegisterPermission("veinminer.blocklist.list." + id, () -> "Allows players to list blocks in the " + category.getId() + " category", PermissionDefault.OP);
            Permission toollistPermission = getOrRegisterPermission("veinminer.toollist.list." + id, () -> "Allows players to list tools in the " + category.getId() + " category", PermissionDefault.OP);

            veinminePermissionParent.getChildren().put(veinminePermission.getName(), true);
            blocklistPermissionParent.getChildren().put(blocklistPermission.getName(), true);
            toollistPermissionParent.getChildren().put(toollistPermission.getName(), true);
        }

        veinminePermissionParent.recalculatePermissibles();
        blocklistPermissionParent.recalculatePermissibles();
        toollistPermissionParent.recalculatePermissibles();
    }

    @NotNull
    private Permission getOrRegisterPermission(@NotNull String permissionName, @NotNull Supplier<String> description, @NotNull PermissionDefault permissionDefault) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Permission permission = pluginManager.getPermission(permissionName);

        if (permission == null) {
            permission = new Permission(permissionName, description.get(), permissionDefault);
            pluginManager.addPermission(permission);
        }

        return permission;
    }

    /**
     * Unregister all tool categories.
     */
    public void unregisterAll() {
        this.categories.clear();
    }

}
