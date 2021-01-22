package wtf.choco.veinminer.api;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import wtf.choco.veinminer.utils.VMConstants;

/**
 * The central management for VeinMiner to handle everything regarding VeinMiner and its features.
 */
public class VeinMinerManager {

    private final BlockList globalBlocklist = new BlockList();
    private final List<@NotNull MaterialAlias> aliases = new ArrayList<>();
    private final Set<@NotNull GameMode> disabledGameModes = EnumSet.noneOf(GameMode.class);

    private final AlgorithmConfig config;

    private final VeinMiner plugin;

    public VeinMinerManager(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.config = new AlgorithmConfig(plugin.getConfig());
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
        Collection<@NotNull ToolCategory> categories = ToolCategory.getAll();
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
    @NotNull
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
    @SuppressWarnings({ "unchecked", "deprecation", "null" })
    public void loadToolCategories() {
        FileConfiguration categoriesConfig = plugin.getCategoriesConfig().asRawConfig();

        for (String key : categoriesConfig.getKeys(false)) {
            ConfigurationSection categoryRoot = categoriesConfig.getConfigurationSection(key);
            if (categoryRoot == null) {
                continue;
            }

            ToolCategory category = new ToolCategory(key, new AlgorithmConfig(categoryRoot, config));
            ToolCategory.register(category);

            List<?> itemsList = categoryRoot.getList("Items");
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
                    Material type = Material.matchMaterial(tool.toString());
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
                    if (meta == null) { // Ignores air
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
                    AlgorithmConfig templateAlgorithmConfig = category.getConfig().clone();
                    templateAlgorithmConfig.readUnsafe(templateRoot); // If I can get rid of this, do it
                    template = new ToolTemplateItemStack(category, item);
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
            Permission blocklistPermissionParent = getOrRegisterPermission(pluginManager, "veinminer.blocklist.list.*");
            Permission toollistPermissionParent = getOrRegisterPermission(pluginManager, "veinminer.toollist.list.*");

            for (ToolCategory category : categories) {
                String id = category.getId().toLowerCase();
                Permission veinminePermission = new Permission("veinminer.veinmine." + id, "Allows players to vein mine using the " + category.getId() + " category", PermissionDefault.TRUE);
                Permission blocklistPermission = new Permission("veinminer.blocklist.list." + id, "Allows players to list blocks in the " + category.getId() + " category", PermissionDefault.OP);
                Permission toollistPermission = new Permission("veinminer.toollist.list." + id, "Allows players to list tools in the " + category.getId() + " category", PermissionDefault.OP);

                veinminePermissionParent.getChildren().put(veinminePermission.getName(), true);
                blocklistPermissionParent.getChildren().put(blocklistPermission.getName(), true);
                toollistPermissionParent.getChildren().put(toollistPermission.getName(), true);
            }

            veinminePermissionParent.recalculatePermissibles();
            blocklistPermissionParent.recalculatePermissibles();
            toollistPermissionParent.recalculatePermissibles();
        }
    }

    /**
     * Load all disabled game modes from the configuration file to memory.
     */
    public void loadDisabledGameModes() {
        this.disabledGameModes.clear();

        this.plugin.getConfig().getStringList(VMConstants.CONFIG_DISABLED_GAME_MODES).forEach(gamemodeString -> {
            Optional<@NotNull GameMode> gamemode = Enums.getIfPresent(GameMode.class, gamemodeString);
            if (!gamemode.isPresent()) {
                return;
            }

            this.disabledGameModes.add(gamemode.get());
        });
    }

    /**
     * Add a game mode to the disabled list.
     *
     * @param gamemode the game mode to add
     */
    public void addDisabledGameMode(GameMode gamemode) {
        this.disabledGameModes.add(gamemode);
    }

    /**
     * Remove a game mode from the disabled list.
     *
     * @param gamemode the game mode to remove
     */
    public void removeDisabledGameMode(GameMode gamemode) {
        this.disabledGameModes.remove(gamemode);
    }

    /**
     * Check whether or not the specific game mode is disabled.
     *
     * @param gamemode the game mode to check
     *
     * @return true if disabled, false otherwise
     */
    public boolean isDisabledGameMode(GameMode gamemode) {
        return disabledGameModes.contains(gamemode);
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
        for (MaterialAlias alias : aliases) {
            if (alias.isAliased(data)) {
                return alias;
            }
        }

        return null;
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
        for (MaterialAlias alias : aliases) {
            if (alias.isAliased(material)) {
                return alias;
            }
        }

        return null;
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

        for (String aliasList : plugin.getConfig().getStringList(VMConstants.CONFIG_ALIASES)) {
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
        this.disabledGameModes.clear();
    }

    @Nullable
    private Material getMaterialKey(@NotNull Map<String, Object> map) {
        Preconditions.checkArgument(map != null, "map must not be null");

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
        Preconditions.checkArgument(manager != null, "manager must not be null");
        Preconditions.checkArgument(permissionName != null, "permissionName must not be null");

        Permission permission = manager.getPermission(permissionName);
        if (permission == null) {
            permission = new Permission(permissionName, PermissionDefault.OP);
            manager.addPermission(permission);
        }

        return permission;
    }

}
