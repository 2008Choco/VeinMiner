package wtf.choco.veinminer.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;

/**
 * The central management for VeinMiner to handle everything regarding VeinMiner and its features.
 */
public class VeinMinerManager {

	private final Set<VeinBlock> veinmineable = new HashSet<>();
	private final List<MaterialAlias> aliases = new ArrayList<>();
	private final Set<UUID> disabledWorlds = new HashSet<>();

	private final VeinMiner plugin;

	public VeinMinerManager(VeinMiner plugin) {
		this.plugin = plugin;
	}

	/**
	 * Load all veinable blocks from the configuration file to memory.
	 */
	public void loadVeinableBlocks() {
		for (String tool : plugin.getConfig().getConfigurationSection("BlockList").getKeys(false)) {
			if (tool.equalsIgnoreCase("all")) continue;

			List<String> blocks = plugin.getConfig().getStringList("BlockList." + tool);

			ToolCategory toolCategory = ToolCategory.getByName(tool);
			boolean all = tool.equalsIgnoreCase("all");

			for (String value : blocks) {
				// Material information
				BlockData data;
				boolean specificData = value.endsWith("]");

				try {
					data = Bukkit.createBlockData(value);
				} catch (IllegalArgumentException e) {
					this.plugin.getLogger().warning("Unknown block type (was it an item?) and/or block states. " + value);
					continue;
				}

				// Registration (ugly, but it has to be this way)
				if (isVeinmineable(data)) {
					VeinBlock block = getVeinmineableBlock(data);

					if (all) {
						for (ToolCategory localTool : ToolCategory.values()) {
							block.setVeinmineableBy(localTool, true);
						}

						continue;
					}

					block.setVeinmineableBy(toolCategory, true);
				} else {
					ToolCategory[] tools = all ? ToolCategory.values() : new ToolCategory[] { toolCategory };

					if (specificData) {
						this.registerVeinmineableBlock(data, tools);
					} else {
						this.registerVeinmineableBlock(data.getMaterial(), tools);
					}
				}
			}
		}
	}

	public VeinBlock registerVeinmineableBlock(BlockData data, String rawData, ToolCategory... tools) {
		Preconditions.checkNotNull(data, "data");

		VeinBlock existing = getVeinmineableBlock(data);
		if (existing != null) {
			for (ToolCategory tool : tools) {
				existing.setVeinmineableBy(tool, true);
			}

			return existing;
		}

		VeinBlock block = new VeinBlock(data, rawData, tools);
		this.veinmineable.add(block);
		return block;
	}

	@Deprecated
	public VeinBlock registerVeinmineableBlock(BlockData data, ToolCategory... tools) {
		return registerVeinmineableBlock(data, data.getAsString(), tools);
	}

	public VeinBlock registerVeinmineableBlock(Material material, ToolCategory... tools) {
		Preconditions.checkNotNull(material, "Cannot register a null material");

		VeinBlock existing = getVeinmineableBlock(material);
		if (existing != null) {
			for (ToolCategory tool : tools) {
				existing.setVeinmineableBy(tool, true);
			}

			return existing;
		}

		VeinBlock block = new VeinBlock(material, tools);
		this.veinmineable.add(block);
		return block;
	}

	public void unregisterVeinmineableBlock(VeinBlock block) {
		this.veinmineable.remove(block);
	}

	public VeinBlock getVeinmineableBlock(BlockData data) {
		// Search for wildcarded first
		VeinBlock wildcarded = getVeinmineableBlock(data.getMaterial());
		if (wildcarded != null) return wildcarded;

		// Now search for specific data
		return veinmineable.stream()
			.filter(b -> b.isSimilar(data))
			.findFirst().orElse(null);
	}

	public VeinBlock getVeinmineableBlock(Material material) {
		return veinmineable.stream()
			.filter(b -> b.getType() == material)
			.findFirst().orElse(null);
	}

	public boolean isVeinmineableBy(BlockData data, ToolCategory tool) {
		VeinBlock block = getVeinmineableBlock(data);
		return block != null && block.isVeinmineableBy(tool);
	}

	public boolean isVeinmineableBy(Material material, ToolCategory tool) {
		VeinBlock block = getVeinmineableBlock(material);
		return block != null && block.isVeinmineableBy(tool);
	}

	public boolean isVeinmineable(BlockData data) {
		return getVeinmineableBlock(data) != null;
	}

	public boolean isVeinmineable(Material material) {
		return getVeinmineableBlock(material) != null;
	}

	public Set<VeinBlock> getVeinmineableBlocks(ToolCategory tool) {
		return veinmineable.stream().filter(b -> b.isVeinmineableBy(tool)).collect(Collectors.toSet());
	}

	public Set<VeinBlock> getVeinmineableBlocks() {
		return Collections.unmodifiableSet(veinmineable);
	}

	public void clearVeinmineableBlocks() {
		this.veinmineable.clear();
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
		return disabledWorlds.stream().map(w -> Bukkit.getWorld(w)).collect(Collectors.toSet());
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
				// Material information
				BlockData data;
				boolean specificData = aliasMaterial.endsWith("]");

				try {
					data = Bukkit.createBlockData(aliasMaterial); // Account for 'quotations'
				} catch (IllegalArgumentException e) {
					this.plugin.getLogger().warning("Unknown block type (was it an item?) and/or block states. " + aliasMaterial);
					continue;
				}

				VeinBlock block = getVeinmineableBlock(data);
				if (block == null) {
					if (specificData) {
						block = registerVeinmineableBlock(data);
					} else {
						block = registerVeinmineableBlock(data.getMaterial());
					}
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
		this.veinmineable.clear();
		this.disabledWorlds.clear();
		this.aliases.clear();
	}

}