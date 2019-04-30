package wtf.choco.veinminer.api;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.data.BlockList;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.template.TemplatePrecedence;
import wtf.choco.veinminer.tool.template.TemplateValidator;
import wtf.choco.veinminer.tool.template.ToolTemplate;

/**
 * The central management for VeinMiner to handle everything regarding VeinMiner and its features.
 */
public class VeinMinerManager {

    private TemplateValidator templateValidator = null;
    private final Map<ToolCategory, BlockList> blocklist = new EnumMap<>(ToolCategory.class);
    private final BlockList globalBlocklist = new BlockList();

    private final List<MaterialAlias> aliases = new ArrayList<>();
    private final Set<UUID> disabledWorlds = new HashSet<>();

    private final VeinMiner plugin;

    public VeinMinerManager(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the {@link BlockList} defined for the specified tool category
     *
     * @param category the category for which to get the blocklist. If null, the
     * global blocklist will be returned
     *
     * @return the category's blocklist
     */
    @NotNull
    public BlockList getBlockList(@Nullable ToolCategory category) {
        if (category == null) { // Yea, yea... ternary. Whatever.
            return globalBlocklist;
        }

        // Ideally it should never compute, but just in case
        return blocklist.computeIfAbsent(category, cat -> new BlockList(0));
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
     * {@link #getBlockList(ToolCategory)} or {@link #getBlockListGlobal()}
     *
     * @return get all veinmineable blocks
     *
     * @see #getBlockList(ToolCategory)
     * @see #getBlockListGlobal()
     */
    @NotNull
    public BlockList getAllVeinMineableBlocks() {
        BlockList[] lists = new BlockList[blocklist.size() + 1];

        int index = 0;
        for (BlockList list : blocklist.values()) {
            lists[index++] = list;
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
    public boolean isVeinMineable(@NotNull BlockData data, @Nullable ToolCategory category) {
        return globalBlocklist.contains(data) || getBlockList(category).contains(data);
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
    public boolean isVeinMineable(@NotNull Material material, @Nullable ToolCategory category) {
        return globalBlocklist.contains(material) || getBlockList(category).contains(material);
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

        for (BlockList list : blocklist.values()) {
            if (list.contains(data)) {
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

        for (BlockList list : blocklist.values()) {
            if (list.contains(material)) {
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
    public VeinBlock getVeinBlockFromBlockList(@NotNull BlockData data, @Nullable ToolCategory category) {
        VeinBlock global = globalBlocklist.getVeinBlock(data);
        return (global != null) ? global : getBlockList(category).getVeinBlock(data);
    }

    /**
     * Load all veinable blocks from the configuration file to memory.
     */
    public void loadVeinableBlocks() {
        ConfigurationSection blocklistSection = plugin.getConfig().getConfigurationSection("BlockList");
        if (blocklistSection == null) return;

        for (String tool : blocklistSection.getKeys(false)) {
            ToolCategory category = ToolCategory.getByName(tool);
            if (category == null) {
                if (!tool.equalsIgnoreCase("all")) { // Special case for "all". If all, don't show an error
                    this.plugin.getLogger().warning("Attempted to create blocklist for the non-existent category, " + tool + "... ignoring.");
                }

                continue;
            }

            BlockList blocklist = getBlockList(category);
            List<String> blocks = plugin.getConfig().getStringList("BlockList." + tool);

            for (String value : blocks) {
                VeinBlock block = VeinBlock.fromString(value);
                if (block == null) {
                    this.plugin.getLogger().warning("Unknown block type (was it an item?) and/or block states for blocklist \"" + category.getName() + "\". Given: " + value);
                    continue;
                }

                blocklist.add(block);
            }
        }
    }

    /**
     * Load all tool templates from the configuration file to memory
     */
    public void loadToolTemplates() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection templateSection = config.getConfigurationSection("ToolTemplates");
        if (templateSection == null) {
            this.templateValidator = TemplateValidator.empty();
            return;
        }

        TemplatePrecedence precedence = Enums.getIfPresent(TemplatePrecedence.class, plugin.getConfig().getString("ToolTemplates.Precedence", "CATEGORY_SPECIFIC")).or(TemplatePrecedence.CATEGORY_SPECIFIC);
        TemplateValidator.ValidatorBuilder validatorBuilder = TemplateValidator.withPrecedence(precedence);

        // Category templates
        for (ToolCategory category : TemplateValidator.TEMPLATABLE_CATEGORIES) {
            ConfigurationSection categorySection = templateSection.getConfigurationSection(category.getName());
            if (categorySection == null) continue;

            String typeString = categorySection.getString("Type", "AIR");
            if (typeString == null) continue;

            Material type = Material.matchMaterial(typeString);
            if (type == Material.AIR) continue;

            String name = categorySection.getString("Name", "");
            if (name == null) continue;

            name = ChatColor.translateAlternateColorCodes('&', name);
            List<String> lore = categorySection.getStringList("Lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());

            ToolTemplate template = null;
            if (type != null) {
                if (!category.contains(type)) {
                    this.plugin.getLogger().warning("Invalid material type " + type.getKey() + " for category " + category.getName() + ". Ignoring...");
                    continue;
                }

                template = new ToolTemplate(type, name, lore);
            } else {
                template = new ToolTemplate(category, name, lore);
            }

            validatorBuilder.template(category, template);
        }

        // Global template
        ConfigurationSection globalSection = templateSection.getConfigurationSection("Global");
        if (globalSection != null) {
            String typeString = globalSection.getString("Type", "AIR");
            Material type = Material.matchMaterial(typeString != null ? typeString.toUpperCase() : "DIAMOND_PICKAXE");
            if (type == null) {
                type = Material.DIAMOND_PICKAXE;
            }

            String name = globalSection.getString("Name", "");
            name = (name != null) ? ChatColor.translateAlternateColorCodes('&', name) : name;

            List<String> lore = globalSection.getStringList("Lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
            validatorBuilder.globalTemplate(new ToolTemplate(type, name, lore));
        }

        this.templateValidator = validatorBuilder.build();
    }

    /**
     * Get the template validator instance.
     *
     * @return the template validator
     */
    @Nullable
    public TemplateValidator getTemplateValidator() {
        return templateValidator;
    }

    /**
     * Load all disabled worlds from the configuration file to memory.
     */
    public void loadDisabledWorlds() {
        this.disabledWorlds.clear();

        for (String worldName : plugin.getConfig().getStringList("DisabledWorlds")) {
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                this.plugin.getLogger().info("Unknown world found... \"" + worldName + "\". Ignoring...");
                continue;
            }

            this.disabledWorlds.add(world.getUID());
        }
    }

    /**
     * Check whether a world has VeinMiner disabled or not.
     *
     * @param world the world to check
     *
     * @return true if the world has VeinMiner disabled, false otherwise
     */
    public boolean isDisabledInWorld(@NotNull World world) {
        Preconditions.checkNotNull(world, "Cannot check state of veinminer in null world");
        return disabledWorlds.contains(world.getUID());
    }

    /**
     * Get a set of all worlds in which VeinMiner is disabled. A copy of the set is returned,
     * therefore any changes made to the returned set will not affect the disabled worlds.
     *
     * @return a set of all disabled worlds
     */
    @NotNull
    public Set<World> getDisabledWorlds() {
        return disabledWorlds.stream().map(Bukkit::getWorld).collect(Collectors.toSet());
    }

    /**
     * Disable vein miner in a specific world.
     *
     * @param world the world for which to disable VeinMiner
     */
    public void setDisabledInWorld(@NotNull World world) {
        Preconditions.checkNotNull(world, "Cannot disable veinminer in null world");
        this.disabledWorlds.add(world.getUID());
    }

    /**
     * Enable VeinMiner in a specific world.
     *
     * @param world the world for which to enabled VeinMiner
     */
    public void setEnabledInWorld(@NotNull World world) {
        Preconditions.checkNotNull(world, "Cannot enable veinminer in null world");
        this.disabledWorlds.remove(world.getUID());
    }

    /**
     * Clear all worlds from the blacklist.
     */
    public void clearDisabledWorlds() {
        this.disabledWorlds.clear();
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
        this.templateValidator.clear();
        this.blocklist.values().forEach(BlockList::clear);
        this.blocklist.clear();
        this.globalBlocklist.clear();

        this.disabledWorlds.clear();
        this.aliases.clear();
    }

}