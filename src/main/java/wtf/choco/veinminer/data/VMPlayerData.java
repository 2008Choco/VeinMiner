package wtf.choco.veinminer.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import wtf.choco.veinminer.pattern.PatternDefault;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;

/**
 * Represents a wrapped Player instance holding player-specific data.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class VMPlayerData {

	private static final Map<UUID, VMPlayerData> CACHE = new HashMap<>();

	private VeinMiningPattern pattern = PatternDefault.get();
	private final Set<ToolCategory> disabledCategories = EnumSet.noneOf(ToolCategory.class);

	private final UUID player;

	private VMPlayerData(UUID player) {
		this.player = player;
	}

	/**
	 * Get the player to which this data belongs.
	 *
	 * @return the owning player
	 */
	public OfflinePlayer getPlayer() {
		return Bukkit.getOfflinePlayer(player);
	}

	/**
	 * Get the {@link Player} instance to which this data belongs. If the player is not online,
	 * null will be returned.
	 *
	 * @return the owning player
	 */
	public Player getPlayerOnline() {
		return Bukkit.getPlayer(player);
	}

	/**
	 * Get the pattern set for this player.
	 *
	 * @return the player's vein mining pattern
	 */
	public VeinMiningPattern getPattern() {
		return pattern;
	}

	/**
	 * Set the pattern to be used for this player.
	 *
	 * @param pattern the pattern to set or null if default
	 */
	public void setPattern(VeinMiningPattern pattern) {
		this.pattern = (pattern != null) ? pattern : PatternDefault.get();
	}

	/**
	 * Enable VeinMiner for this player (all categories).
	 */
	public void enableVeinMiner() {
		this.disabledCategories.clear();
	}

	/**
	 * Enable VeinMiner for this player for a specific category.
	 *
	 * @param category the category to enable
	 */
	public void enableVeinMiner(ToolCategory category) {
		Preconditions.checkArgument(category != null, "Cannot enable null category");
		this.disabledCategories.remove(category);
	}

	/**
	 * Disable VeinMiner for this player (all categories).
	 */
	public void disableVeinMiner() {
		for (ToolCategory category : ToolCategory.values()) {
			this.disabledCategories.add(category);
		}
	}

	/**
	 * Disable VeinMiner for this player for a specific category.
	 *
	 * @param category the category to disable
	 */
	public void disableVeinMiner(ToolCategory category) {
		Preconditions.checkArgument(category != null, "Cannot disable null category");
		this.disabledCategories.add(category);
	}

	/**
	 * Set VeinMiner's enabled state for this player (all categories).
	 *
	 * @param enabled whether or not to enable VeinMiner
	 */
	public void setVeinMinerEnabled(boolean enabled) {
		if (enabled) {
			this.enableVeinMiner();
		} else {
			this.disableVeinMiner();
		}
	}

	/**
	 * Set VeinMiner's enabled state for this player for a specific category.
	 *
	 * @param enabled whether or not to enable VeinMiner
	 * @param category the category to enable (or disable)
	 */
	public void setVeinMinerEnabled(boolean enabled, ToolCategory category) {
		if (enabled) {
			this.enableVeinMiner(category);
		} else {
			this.disableVeinMiner(category);
		}
	}

	/**
	 * Check whether or not VeinMiner is enabled for this player (at least one category).
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isVeinMinerEnabled() {
		return disabledCategories.isEmpty();
	}

	/**
	 * Check whether or not VeinMiner is enabled for this player for the specified category.
	 *
	 * @param category the category to check
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isVeinMinerEnabled(ToolCategory category) {
		return !disabledCategories.contains(category);
	}

	/**
	 * Check whether or not VeinMiner is disabled for this player (all categories)
	 *
	 * @return true if disabled, false otherwise
	 */
	public boolean isVeinMinerDisabled() {
		return disabledCategories.size() >= ToolCategory.values().length;
	}

	/**
	 * Check whether or not VeinMiner is disabled for this player for the specified category.
	 *
	 * @param category the category to check
	 *
	 * @return true if disabled, false otherwise
	 */
	public boolean isVeinMinerDisabled(ToolCategory category) {
		return disabledCategories.contains(category);
	}

	/**
	 * Check whether or not VeinMiner is disabled in at least one category. This is effectively
	 * the inverse of {@link #isVeinMinerEnabled()}.
	 *
	 * @return true if at least one category is disabled, false otherwise (all enabled)
	 */
	public boolean isVeinMinerPartiallyDisabled() {
		return !disabledCategories.isEmpty();
	}

	/**
	 * Get the {@link VMPlayerData} instance for the specified player.
	 *
	 * @param player the player whose data to retrieve
	 *
	 * @return the player data
	 */
	public static VMPlayerData get(OfflinePlayer player) {
		Preconditions.checkArgument(player != null, "Cannot get data for null player");
		return CACHE.computeIfAbsent(player.getUniqueId(), VMPlayerData::new);
	}

	/**
	 * Clear the player data cache (all player-specific data will be lost)
	 */
	public static void clearCache() {
		CACHE.clear();
	}

}