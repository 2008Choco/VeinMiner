package wtf.choco.veinminer.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.BlockList;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplate;
import wtf.choco.veinminer.tool.ToolTemplateItemStack;
import wtf.choco.veinminer.tool.ToolTemplateMaterial;

/**
 * The central management for VeinMiner to handle everything regarding VeinMiner and its features.
 */
public class VeinMinerManager {

    private final BlockList globalBlocklist = new BlockList();
    private final List<MaterialAlias> aliases = new ArrayList<>();
    private final AlgorithmConfig config;

    private final VeinMiner plugin;

    public VeinMinerManager(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.config = plugin.createDefaultAlgorithmConfig();
    }

    /**
     * Get the global blocklist. This blocklist represents blocks and states listed by
     * the "All" category in the configuration file.
     *
     * @return the global blocklist
     */
    @NotNull
    public BlockList getBlockListGlobal() {
        return globalBlocklist;
    }

    /**
     * Get a {@link BlockList} of all veinmineable blocks. The returned blocklist will
     * contain unique block-state combinations from all categories and the global blocklist.
     * Any changes made to the returned block list will not affect the underlying blocklist,
     * therefore if any changes are required, they should be done to those returned by
     * {@link ToolCategory#getBlocklist()} or {@link #getBlockListGlobal()}
     *
     * @return get all veinmineable blocks
     *
     * @see ToolCategory#getBlocklist()
     * @see #getBlockListGlobal()
     */
    @NotNull
    public BlockList getAllVeinMineableBlocks() {
        Collection<ToolCategory> categories = ToolCategory.getAll();
        BlockList[] lists = new BlockList[categories.size() + 1];

        int index = 0;
        for (ToolCategory category : categories) {
            lists[index++] = category.getBlocklist();
        }
        lists[index] = globalBlocklist;

        return new BlockList(lists);
    }

    /**
     * Check whether the specified {@link BlockData} is vein mineable for the specified category.
     *
     * @param data the data to check
     * @param category the category to check
     *
     * @return true if the data is vein mineable by the specified category, false otherwise
     *
     * @see VeinBlock#encapsulates(BlockData)
     */
    public boolean isVeinMineable(@NotNull BlockData data, @NotNull ToolCategory category) {
        return globalBlocklist.contains(data) || category.getBlocklist().contains(data);
    }

    /**
     * Check whether the specified {@link Material} is vein mineable for the specified category.
     *
     * @param material the material to check
     * @param category the category to check
     *
     * @return true if the material is vein mineable by the specified category, false otherwise
     *
     * @see VeinBlock#encapsulates(Material)
     */
    public boolean isVeinMineable(@NotNull Material material, @NotNull ToolCategory category) {
        return globalBlocklist.contains(material) || category.getBlocklist().contains(material);
    }

    /**
     * Check whether the specified {@link BlockData} is at all vein mineable.
     *
     * @param data the data to check
     *
     * @return true if the data is vein mineable, false otherwise
     *
     * @see VeinBlock#encapsulates(BlockData)
     */
    public boolean isVeinMineable(@NotNull BlockData data) {
        if (globalBlocklist.contains(data)) {
            return true;
        }

        for (ToolCategory category : ToolCategory.getAll()) {
            if (category.getBlocklist().contains(data)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether the specified {@link Material} is at all vein mineable.
     *
     * @param material the material to check
     *
     * @return true if the material is vein mineable, false otherwise
     *
     * @see VeinBlock#encapsulates(Material)
     */
    public boolean isVeinMineable(@NotNull Material material) {
        if (globalBlocklist.contains(material)) {
            return true;
        }

        for (ToolCategory category : ToolCategory.getAll()) {
            if (category.getBlocklist().contains(material)) {
                return true;
            }
        }

        return false;
    }

    /**
     * See {@link BlockList#getVeinBlock(BlockData)}. This search includes the specified category
     * as well as the global blocklist.
     *
     * @param data the block data for which to get a VeinBlock
     * @param category the category blocklist in which to retrieve a VeinBlock
     *
     * @return the vein block. null if none
     */
    @Nullable
    public VeinBlock getVeinBlockFromBlockList(@NotNull BlockData data, @NotNull ToolCategory category) {
        VeinBlock global = globalBlocklist.getVeinBlock(data);
        return (global != null) ? global : category.getBlocklist().getVeinBlock(data);
    }

    /**
     * Get the global algorithm config.
     *
     * @return global algorithm config
     */
    public AlgorithmConfig getConfig() {
        return config;
    }

    /**
     * Load all veinable blocks from the configuration file to memory.
     */
    public void loadVeinableBlocks() {
        ConfigurationSection blocklistSection = plugin.getConfig().getConfigurationSection("BlockList");
        if (blocklistSection == null) {
            return;
        }

        for (String tool : blocklistSection.getKeys(false)) {
            ToolCategory category = ToolCategory.get(tool);
            if (category == null) {
                if (!tool.equalsIgnoreCase("all") && !tool.equalsIgnoreCase("hand")) { // Special case for "all" and "hand". Don't show an error
                    this.plugin.getLogger().warning("Attempted to create blocklist for the non-existent category, " + tool + "... ignoring.");
                }

                continue;
            }

            BlockList blocklist = category.getBlocklist();
            List<String> blocks = plugin.getConfig().getStringList("BlockList." + tool);

            for (String value : blocks) {
                VeinBlock block = VeinBlock.fromString(value);
                if (block == null) {
                    this.plugin.getLogger().warning("Unknown block type (was it an item?) and/or block states for blocklist \"" + category.getId() + "\". Given: " + value);
                    continue;
                }

                blocklist.add(block);
            }
        }
    }

    /**
     * Load all tool categories from the configuration file to memory.
     */
    @SuppressWarnings("unchecked")
    public void loadToolCategories() {
        FileConfiguration categoriesConfig = plugin.getCategoriesConfig().asRawConfig();

        for (String key : categoriesConfig.getKeys(false)) {
            ToolCategory category = new ToolCategory(this, key);
            ConfigurationSection root = categoriesConfig.getConfigurationSection(key);
            if (root == null) {
                continue;
            }

            // Per-category configuration
            AlgorithmConfig algorithmConfig = category.getConfig();
            if (root.contains("RepairFriendlyVeinMiner")) {
                algorithmConfig.repairFriendly(root.getBoolean("RepairFriendlyVeinMiner"));
            }
            if (root.contains("IncludeEdges")) {
                algorithmConfig.includeEdges(root.getBoolean("IncludeEdges"));
            }
            if (root.contains("MaxVeinSize")) {
                algorithmConfig.maxVeinSize(Math.max(root.getInt("MaxVeinSize"), 1));
            }
            if (root.contains("Cost")) {
                algorithmConfig.cost(Math.max(root.getDouble("Cost"), 0.0));
            }
            if (root.contains("DisabledWorlds")) {
                for (String worldName : root.getStringList("DisabledWorlds")) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    algorithmConfig.disabledWorld(world);
                }
            }

            List<?> itemsList = root.getList("Items");
            if (itemsList == null) {
                this.plugin.getLogger().severe("No item list provided for category with ID " + category.getId());
                continue;
            }

            // Tool configuration
            for (Object tool : itemsList) {
                ToolTemplate template = null;
                if (tool == null) {
                    continue;
                }

                if (tool instanceof String) {
                    Material type = Material.matchMaterial((String) tool);
                    if (type == null) {
                        this.plugin.getLogger().warning("Unknown material of type \"" + tool + "\" provided, ignoring...");
                        continue;
                    }

                    template = new ToolTemplateMaterial(category, type);
                } else if (tool instanceof Map) {
                    Map<String, Object> templateRoot = (Map<String, Object>) tool;

                    // Material value
                    Material material = getMaterialKey(templateRoot);
                    if (material == null) {
                        this.plugin.getLogger().warning("Tried to create item with missing or invalid type... material must be declared");
                        continue;
                    }

                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) { // Stupid nullability annotations :(
                        continue;
                    }

                    // Additional meta
                    Object name = templateRoot.get("Name");
                    Object lore = templateRoot.get("Lore");

                    if (name instanceof String) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.toString()));
                    }

                    if (lore instanceof String) {
                        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', (String) lore)));
                    } else if (lore instanceof List) {
                        List<String> loreList = ((List<Object>) lore).stream().filter(o -> o instanceof String)
                            .map(s -> ChatColor.translateAlternateColorCodes('&', (String) s)).collect(Collectors.toList());
                        if (loreList.isEmpty()) {
                            continue;
                        }

                        meta.setLore(loreList);
                    }

                    item.setItemMeta(meta);
                    template = new ToolTemplateItemStack(category, item);

                    // Template configuration
                    AlgorithmConfig config = template.getConfig();

                    Object repairFriendlyVeinMiner = templateRoot.get("RepairFriendlyVeinMiner");
                    Object includeEdges = templateRoot.get("IncludeEdges");
                    Object maxVeinSize = templateRoot.get("MaxVeinSize");
                    Object cost = templateRoot.get("Cost");
                    Object disabledWorlds = templateRoot.get("DisabledWorlds");

                    if (repairFriendlyVeinMiner instanceof Boolean) {
                        config.repairFriendly((boolean) repairFriendlyVeinMiner);
                    }
                    if (includeEdges instanceof Boolean) {
                        config.repairFriendly((boolean) includeEdges);
                    }
                    if (maxVeinSize instanceof Integer) {
                        config.maxVeinSize(Math.max((int) maxVeinSize, 1));
                    }
                    if (cost instanceof Number) {
                        config.cost(Math.max((double) cost, 0.0));
                    }
                    if (disabledWorlds instanceof List) {
                        ((List<Object>) disabledWorlds).stream().filter(o -> o instanceof String).map(s -> UUID.fromString((String) s))
                            .distinct().map(Bukkit::getWorld).forEach(w -> {
                                if (w == null) return;
                                config.disabledWorld(w);
                            });
                    }
                }

                if (template != null) {
                    category.addTool(template);
                }
            }
        }

        // Handle dynamic permissions
        Collection<ToolCategory> categories = ToolCategory.getAll();
        if (categories.size() >= 1) {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Permission veinminePermissionParent = getOrRegisterPermission(pluginManager, "veinminer.veinmine.*");
            Permission listPermissionParent = getOrRegisterPermission(pluginManager, "veinminer.blocklist.list.*");

            for (ToolCategory category : categories) {
                String id = category.getId().toLowerCase();
                Permission veinminePermission = new Permission("veinminer.veinmine." + id, "Allows players to vein mine using the " + category.getId() + " category", PermissionDefault.OP);
                Permission listPermission = new Permission("veinminer.blocklist.list." + id, "Allows players to list blocks in the " + category.getId() + " category", PermissionDefault.OP);

                veinminePermissionParent.getChildren().put(veinminePermission.getName(), true);
                listPermissionParent.getChildren().put(listPermission.getName(), true);
            }

            veinminePermissionParent.recalculatePermissibles();
            listPermissionParent.recalculatePermissibles();
        }
    }

    /**
     * Register a new MaterialAlias.
     *
     * @param alias the alias to register
     */
    public void registerAlias(@NotNull MaterialAlias alias) {
        Preconditions.checkNotNull(alias, "Cannot register a null alias");
        this.aliases.add(alias);
    }

    /**
     * Unregister a MaterialAlias.
     *
     * @param alias the alias to unregister
     */
    public void unregisterAlias(@NotNull MaterialAlias alias) {
        this.aliases.remove(alias);
    }

    /**
     * Get the alias associated with a specific block data.
     *
     * @param data the block data to reference
     *
     * @return the associated alias. null if none
     */
    @Nullable
    public MaterialAlias getAliasFor(@NotNull BlockData data) {
        return aliases.stream().filter(a -> a.isAliased(data)).findFirst().orElse(null);
    }

    /**
     * Get the alias associated with a specific material.
     *
     * @param material the material to reference
     *
     * @return the associated alias. null if none
     */
    @Nullable
    public MaterialAlias getAliasFor(@NotNull Material material) {
        return aliases.stream().filter(a -> a.isAliased(material)).findFirst().orElse(null);
    }

    /**
     * Get the alias associated with a block. An alias will be retrieved by the block's type,
     * and if none, the block's data.
     *
     * @param block the block to reference
     *
     * @return the associated alias. null if none
     */
    @Nullable
    public MaterialAlias getAliasFor(@NotNull Block block) {
        MaterialAlias alias = getAliasFor(block.getType());
        return (alias != null) ? alias : getAliasFor(block.getBlockData());
    }

    /**
     * Load all material aliases from config to memory.
     */
    public void loadMaterialAliases() {
        this.aliases.clear();

        for (String aliasList : plugin.getConfig().getStringList("Aliases")) {
            MaterialAlias alias = new MaterialAlias();

            for (String aliasState : aliasList.split("\\s*,\\s*")) {
                VeinBlock block = VeinBlock.fromString(aliasState);
                if (block == null) {
                    this.plugin.getLogger().warning("Unknown block type (was it an item?) and/or block states for alias \"" + aliasList + "\". Given: " + aliasState);
                    continue;
                }

                alias.addAlias(block);
            }

            this.aliases.add(alias);
        }
    }

    /**
     * Clear all localised data in the VeinMiner Manager.
     */
    public void clearLocalisedData() {
        this.globalBlocklist.clear();
        this.aliases.clear();
    }

    @Nullable
    private Material getMaterialKey(Map<String, Object> map) {
        Object possibleMapping = map.get("Material");
        if (possibleMapping instanceof String) {
            return Material.matchMaterial((String) possibleMapping);
        }

        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                return Material.matchMaterial(entry.getKey());
            }
        }

        return Material.AIR;
    }

    @NotNull
    private Permission getOrRegisterPermission(@NotNull PluginManager manager, @NotNull String permissionName) {
        Permission permission = manager.getPermission(permissionName);
        if (permission == null) {
            permission = new Permission(permissionName, PermissionDefault.OP);
            manager.addPermission(permission);
        }

        return permission;
    }

}
