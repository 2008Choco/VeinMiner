package wtf.choco.veinminer.api;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.data.BlockList;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplate;

/**
 * The central management for VeinMiner to handle everything regarding VeinMiner and its features.
 */
public class VeinMinerManager {

	private final Map<ToolCategory, ToolTemplate> toolTemplates = new EnumMap<>(ToolCategory.class);
	private final Map<ToolCategory, BlockList> blocklist = new EnumMap<>(ToolCategory.class);
	private final BlockList globalBlocklist = new BlockList();

	private final List<MaterialAlias> aliases = new ArrayList<>();
	private final Set<UUID> disabledWorlds = new HashSet<>();

	private final VeinMiner plugin;

	public VeinMinerManager(VeinMiner plugin) {
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
	public BlockList getBlockList(ToolCategory category) {
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
	public boolean isVeinMineable(BlockData data, ToolCategory category) {
		return globalBlocklist.contains(data) || blocklist.get(category).contains(data);
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
	public boolean isVeinMineable(Material material, ToolCategory category) {
		return globalBlocklist.contains(material) || blocklist.get(category).contains(material);
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
	public boolean isVeinMineable(BlockData data) {
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
	public boolean isVeinMineable(Material material) {
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
	 * Load all veinable blocks from the configuration file to memory.
	 */
	public void loadVeinableBlocks() {
		for (String tool : plugin.getConfig().getConfigurationSection("BlockList").getKeys(false)) {
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
				VeinBlock block = getVeinBlockFromString(value);
				if (block == null) continue;

				blocklist.add(block);
			}
		}
	}

	/**
	 * Load all tool templates from the configuration file to memory
	 */
	public void loadToolTemplates() {
		this.toolTemplates.clear();

		FileConfiguration config = plugin.getConfig();
		for (String categoryName : config.getConfigurationSection("Tools").getKeys(false)) {
			ToolCategory category = ToolCategory.getByName(categoryName);

			ConfigurationSection categoryTemplate = config.getConfigurationSection("Tools." + categoryName + "Tool");
			if (categoryTemplate == null) continue;

			Material type = Material.matchMaterial(categoryTemplate.getString("Type"));
			String name = ChatColor.translateAlternateColorCodes('&', categoryTemplate.getString("Name", ""));
			List<String> lore = categoryTemplate.getStringList("Lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());

			ToolTemplate template = null;
			if (type != null) {
				if (!category.contains(type)) {
					this.plugin.getLogger().warning("Invalid material type " + type.getKey() + " for category " + category.getName() + ". Ignoring...");
					continue;
				}

				template = new ToolTemplate(type, (name.isEmpty()) ? null : name, (lore.isEmpty()) ? null : lore);
			} else {
				template = new ToolTemplate(category, (name.isEmpty()) ? null : name, (lore.isEmpty()) ? null : lore);
			}

			this.toolTemplates.put(category, template);
		}
	}

	/**
	 * Set the tool template for the specified category. If null, the template will be removed
	 * and default behaviour will be used.
	 *
	 * @param category the category for which to set a template
	 * @param template the template to set, or null for none
	 */
	public void setToolTemplate(ToolCategory category, ToolTemplate template) {
		Preconditions.checkArgument(category != null, "Cannot set template for null category");
		Preconditions.checkArgument(category.canHaveToolTemplate(), "The provided category (%s) cannot define a tool template", category.getName());

		this.toolTemplates.put(category, (template != null) ? template : ToolTemplate.empty(category));
	}

	/**
	 * Get the tool template used for the specified category. If no template is specified, an empty
	 * template will be returned (not null, but always true).
	 *
	 * @param category the category whose template to get
	 *
	 * @return the category's template
	 */
	public ToolTemplate getToolTemplate(ToolCategory category) {
		return toolTemplates.computeIfAbsent(category, ToolTemplate::empty);
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
	public boolean isDisabledInWorld(World world) {
		Preconditions.checkNotNull(world, "Cannot check state of veinminer in null world");
		return disabledWorlds.contains(world.getUID());
	}

	/**
	 * Get a set of all worlds in which VeinMiner is disabled. A copy of the set is returned,
	 * therefore any changes made to the returned set will not affect the disabled worlds.
	 *
	 * @return a set of all disabled worlds
	 */
	public Set<World> getDisabledWorlds() {
		return disabledWorlds.stream().map(Bukkit::getWorld).collect(Collectors.toSet());
	}

	/**
	 * Disable vein miner in a specific world.
	 *
	 * @param world the world for which to disable VeinMiner
	 */
	public void setDisabledInWorld(World world) {
		Preconditions.checkNotNull(world, "Cannot disable veinminer in null world");
		this.disabledWorlds.add(world.getUID());
	}

	/**
	 * Enable VeinMiner in a specific world.
	 *
	 * @param world the world for which to enabled VeinMiner
	 */
	public void setEnabledInWorld(World world) {
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
	public void registerAlias(MaterialAlias alias) {
		Preconditions.checkNotNull(alias, "Cannot register a null alias");
		this.aliases.add(alias);
	}

	/**
	 * Unregister a MaterialAlias.
	 *
	 * @param alias the alias to unregister
	 */
	public void unregisterAlias(MaterialAlias alias) {
		this.aliases.remove(alias);
	}

	/**
	 * Get the alias associated with a specific block data.
	 *
	 * @param data the block data to reference
	 *
	 * @return the associated alias. null if none
	 */
	public MaterialAlias getAliasFor(BlockData data) {
		return aliases.stream().filter(a -> a.isAliased(data)).findFirst().orElse(null);
	}

	/**
	 * Get the alias associated with a specific material.
	 *
	 * @param material the material to reference
	 *
	 * @return the associated alias. null if none
	 */
	public MaterialAlias getAliasFor(Material material) {
		return aliases.stream().filter(a -> a.isAliased(material)).findFirst().orElse(null);
	}

	/**
	 * Load all material aliases from config to memory.
	 */
	public void loadMaterialAliases() {
		this.aliases.clear();

		for (String aliasList : plugin.getConfig().getStringList("Aliases")) {
			MaterialAlias alias = new MaterialAlias();

			for (String aliasMaterial : aliasList.split("\\s*,\\s*")) {
				VeinBlock block = getVeinBlockFromString(aliasMaterial);
				if (block == null) continue;

				alias.addAlias(block);
			}

			this.aliases.add(alias);
		}
	}

	/**
	 * Clear all localised data in the VeinMiner Manager.
	 */
	public void clearLocalisedData() {
		this.toolTemplates.clear();
		this.blocklist.values().forEach(BlockList::clear);
		this.blocklist.clear();
		this.globalBlocklist.clear();

		this.disabledWorlds.clear();
		this.aliases.clear();
	}

	private VeinBlock getVeinBlockFromString(String dataString) {
		Matcher matcher = VeinMiner.BLOCK_DATA_PATTERN.matcher(dataString);
		if (!matcher.find()) {
			this.plugin.getLogger().warning("Invalid block data format provided (" + dataString + ")... must be defined as (for example) \"minecraft:chest[waterlogged=true]\"");
			return null;
		}

		BlockData data;
		boolean specificData = matcher.groupCount() >= 1;

		try {
			data = Bukkit.createBlockData(matcher.group()); // Use what the matcher found to make the life of the parser easier
		} catch (IllegalArgumentException e) {
			this.plugin.getLogger().warning("Unknown block type (was it an item?) and/or block states. " + dataString);
			return null;
		}

		return (specificData) ? VeinBlock.get(data) : VeinBlock.get(data.getMaterial());
	}

}