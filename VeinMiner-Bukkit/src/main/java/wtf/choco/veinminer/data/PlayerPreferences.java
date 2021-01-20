package wtf.choco.veinminer.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.tool.ToolCategory;

/**
 * Represents a wrapped Player instance holding player-specific data.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class PlayerPreferences {

    private static final Map<@NotNull UUID, @NotNull PlayerPreferences> CACHE = new HashMap<>();

    private ActivationStrategy activationStrategy = ActivationStrategy.getDefaultActivationStrategy();
    private final Set<@NotNull ToolCategory> disabledCategories = new HashSet<>();
    private boolean dirty = false;

    private final UUID player;

    private PlayerPreferences(@NotNull UUID player) {
        this.player = player;
    }

    /**
     * Get the UUID of the player to which this data belongs.
     *
     * @return the owning player uuid
     */
    @NotNull
    public UUID getPlayerUUID() {
        return player;
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
        this.dirty = !disabledCategories.isEmpty();
        this.disabledCategories.clear();
    }

    /**
     * Enable VeinMiner for this player for a specific category.
     *
     * @param category the category to enable
     */
    public void enableVeinMiner(@NotNull ToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot enable null category");
        this.dirty = disabledCategories.remove(category);
    }

    /**
     * Disable VeinMiner for this player (all categories).
     */
    public void disableVeinMiner() {
        this.dirty = disabledCategories.addAll(ToolCategory.getAll());
    }

    /**
     * Disable VeinMiner for this player for a specific category.
     *
     * @param category the category to disable
     */
    public void disableVeinMiner(@NotNull ToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot disable null category");
        this.dirty = disabledCategories.add(category);
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
     * Set the activation strategy to use for this player.
     *
     * @param activationStrategy the activation strategy
     */
    public void setActivationStrategy(@NotNull ActivationStrategy activationStrategy) {
        Preconditions.checkArgument(activationStrategy != null, "activationStrategy must not be null");

        this.dirty = (this.activationStrategy != activationStrategy);
        this.activationStrategy = activationStrategy;
    }

    /**
     * Get the activation strategy to use for this player.
     *
     * @return the activation strategy
     */
    @NotNull
    public ActivationStrategy getActivationStrategy() {
        return activationStrategy;
    }

    /**
     * Set whether or not this player data should be written.
     *
     * @param dirty true if dirty, false otherwise
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Check whether or not this player data has been modified since last write.
     *
     * @return true if modified, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Write this object's data into the provided JsonObject.
     *
     * @param root the object in which to write the data
     *
     * @return the modified instance of the provided object
     */
    @NotNull
    public JsonObject write(@NotNull JsonObject root) {
        Preconditions.checkArgument(root != null, "root must not be null");

        root.addProperty("activation_strategy", activationStrategy.name());

        JsonArray disabledCategoriesArray = new JsonArray();
        this.disabledCategories.forEach(category -> disabledCategoriesArray.add(category.getId()));
        root.add("disabled_categories", disabledCategoriesArray);

        return root;
    }

    /**
     * Read data from the provided JsonObject into this object.
     *
     * @param root the object from which to read data
     */
    public void read(@NotNull JsonObject root) {
        Preconditions.checkArgument(root != null, "root must not be null");

        if (root.has("activation_strategy")) {
            this.activationStrategy = ActivationStrategy.getByName(root.get("activation_strategy").getAsString());
            if (activationStrategy == null) {
                this.activationStrategy = ActivationStrategy.SNEAK;
            }
        }

        if (root.has("disabled_categories")) {
            this.disabledCategories.clear();

            JsonArray disabledCategoriesArray = root.getAsJsonArray("disabled_categories");
            disabledCategoriesArray.forEach(element -> {
                ToolCategory category = ToolCategory.get(element.getAsString());
                if (category == null) {
                    return;
                }

                this.disabledCategories.add(category);
            });
        }

        this.dirty = false;
    }

    /**
     * Write this object to its file in the specified directory.
     *
     * @param directory the directory in which the file resides
     *
     * @see VeinMiner#getPlayerDataDirectory()
     */
    public void writeToFile(@NotNull File directory) {
        Preconditions.checkArgument(directory != null && directory.isDirectory(), "directory must be a directory");

        File file = new File(directory, player + ".json");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            VeinMiner.GSON.toJson(write(new JsonObject()), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read this object from its file in the specified directory.
     *
     * @param directory the directory in which the file resides
     *
     * @see VeinMiner#getPlayerDataDirectory()
     */
    public void readFromFile(@NotNull File directory) {
        Preconditions.checkArgument(directory != null && directory.isDirectory(), "directory must be a directory");

        File file = new File(directory, player + ".json");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonObject root = VeinMiner.GSON.fromJson(reader, JsonObject.class);
            this.read(root);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            OfflinePlayer player = getPlayer();
            if (player != null) {
                VeinMiner.getPlugin().getLogger().warning("Could not read player data for user " + player.getName() + " (" + getPlayerUUID() + "). Invalid file format. Deleting...");
            }

            file.delete();
        }
    }

    /**
     * Get the {@link PlayerPreferences} instance for the specified player.
     *
     * @param player the player whose data to retrieve
     *
     * @return the player data
     */
    @NotNull
    public static PlayerPreferences get(@NotNull OfflinePlayer player) {
        Preconditions.checkArgument(player != null, "Cannot get data for null player");
        return CACHE.computeIfAbsent(player.getUniqueId(), PlayerPreferences::new);
    }

    /**
     * Clear the player data cache (all player-specific data will be lost)
     */
    public static void clearCache() {
        CACHE.clear();
    }

}
