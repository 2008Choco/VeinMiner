package wtf.choco.veinminer.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.tool.ToolCategory;

/**
 * Represents a wrapped Player instance holding player-specific data.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class VMPlayerData {

    private static final Map<UUID, VMPlayerData> CACHE = new HashMap<>();

    private final Set<ToolCategory> disabledCategories = new HashSet<>();

    private final UUID player;

    private VMPlayerData(@NotNull UUID player) {
        this.player = player;
    }

    /**
     * Get the player to which this data belongs.
     *
     * @return the owning player
     */
    @Nullable
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }

    /**
     * Get the {@link Player} instance to which this data belongs. If the player is not online,
     * null will be returned.
     *
     * @return the owning player
     */
    @Nullable
    public Player getPlayerOnline() {
        return Bukkit.getPlayer(player);
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
    public void enableVeinMiner(@NotNull ToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot enable null category");
        this.disabledCategories.remove(category);
    }

    /**
     * Disable VeinMiner for this player (all categories).
     */
    public void disableVeinMiner() {
        this.disabledCategories.addAll(ToolCategory.getAll());
    }

    /**
     * Disable VeinMiner for this player for a specific category.
     *
     * @param category the category to disable
     */
    public void disableVeinMiner(@NotNull ToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot disable null category");
        this.disabledCategories.add(category);
    }

    /**
     * Set VeinMiner's enabled state for this player (all categories).
     *
     * @param enable whether or not to enable VeinMiner
     */
    public void setVeinMinerEnabled(boolean enable) {
        if (enable) {
            this.enableVeinMiner();
        } else {
            this.disableVeinMiner();
        }
    }

    /**
     * Set VeinMiner's enabled state for this player for a specific category.
     *
     * @param enable whether or not to enable VeinMiner
     * @param category the category to enable (or disable)
     */
    public void setVeinMinerEnabled(boolean enable, @NotNull ToolCategory category) {
        if (enable) {
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
    public boolean isVeinMinerEnabled(@NotNull ToolCategory category) {
        return !disabledCategories.contains(category);
    }

    /**
     * Check whether or not VeinMiner is disabled for this player (all categories)
     *
     * @return true if disabled, false otherwise
     */
    public boolean isVeinMinerDisabled() {
        return disabledCategories.size() >= ToolCategory.getRegisteredAmount();
    }

    /**
     * Check whether or not VeinMiner is disabled for this player for the specified category.
     *
     * @param category the category to check
     *
     * @return true if disabled, false otherwise
     */
    public boolean isVeinMinerDisabled(@NotNull ToolCategory category) {
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
    public static VMPlayerData get(@NotNull OfflinePlayer player) {
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
