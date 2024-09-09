package wtf.choco.veinminer.data;

import com.google.common.base.Enums;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * An implementation of {@link PersistentDataStorage} for JSON files in a directory.
 */
final class PersistentDataStorageJSON implements PersistentDataStorage {

    private final PersistentStorageType type;
    private final VeinMinerPlugin plugin;
    private final File directory;
    private final Gson gson;

    PersistentDataStorageJSON(@NotNull PersistentStorageType type, @NotNull VeinMinerPlugin plugin, @NotNull File directory) {
        this.type = type;
        this.plugin = plugin;
        this.directory = directory;
        this.gson = new Gson();
    }

    @NotNull
    @Override
    public PersistentStorageType getType() {
        return type;
    }

    @NotNull
    @Override
    public CompletableFuture<Void> init() {
        this.directory.mkdirs();
        return CompletableFuture.completedFuture(null);
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> savePlayer(player));
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> save(@NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty() || players.stream().allMatch(player -> !player.isDirty())) {
            return CompletableFuture.completedFuture(new ArrayList<>(players));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>(players.size());
            players.forEach(player -> result.add(savePlayer(player)));
            return result;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> loadPlayer(player));
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> load(@NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>(players.size());
            players.forEach(player -> result.add(loadPlayer(player)));
            return result;
        });
    }

    private VeinMinerPlayer savePlayer(VeinMinerPlayer player) {
        try {
            File playerFile = new File(directory, player.getPlayerUUID().toString() + ".json");
            playerFile.createNewFile();

            JsonObject root = new JsonObject();
            root.addProperty("activation_strategy_id", player.getActivationStrategy().name());
            root.addProperty("vein_mining_pattern_id", player.getVeinMiningPattern().getKey().toString());

            JsonArray disabledCategoriesArray = new JsonArray();
            player.getDisabledCategories().forEach(category -> disabledCategoriesArray.add(category.getId()));
            root.add("disabled_categories", disabledCategoriesArray);

            Files.write(playerFile.toPath(), gson.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CompletionException(e);
        }

        return player;
    }

    private VeinMinerPlayer loadPlayer(VeinMinerPlayer player) {
        File playerFile = new File(directory, player.getPlayerUUID().toString() + ".json");

        if (!playerFile.exists()) {
            return player;
        }

        try (BufferedReader reader = Files.newBufferedReader(playerFile.toPath(), StandardCharsets.UTF_8)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            if (root.has("activation_strategy_id")) {
                player.setActivationStrategy(Enums.getIfPresent(ActivationStrategy.class, root.get("activation_strategy_id").getAsString().toUpperCase()).or(plugin.getConfiguration().getDefaultActivationStrategy()));
            }

            if (root.has("disabled_categories")) {
                player.setVeinMinerEnabled(true); // Ensure that all categories are loaded again

                root.getAsJsonArray("disabled_categories").forEach(element -> {
                    if (!element.isJsonPrimitive()) {
                        return;
                    }

                    VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(element.getAsString().toUpperCase());
                    if (category == null) {
                        return;
                    }

                    player.setVeinMinerEnabled(category, false);
                });
            }

            if (root.has("vein_mining_pattern_id")) {
                player.setVeinMiningPattern(plugin.getPatternRegistry().getOrDefault(root.get("vein_mining_pattern_id").getAsString(), plugin.getConfiguration().getDefaultVeinMiningPattern()), false);
            }
        } catch (IOException | JsonSyntaxException e) {
            throw new CompletionException(e);
        }

        return player;
    }

}
