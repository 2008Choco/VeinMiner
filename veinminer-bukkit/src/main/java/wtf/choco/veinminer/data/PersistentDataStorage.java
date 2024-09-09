package wtf.choco.veinminer.data;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.player.VeinMinerPlayer;

/**
 * A means of storing persistent {@link VeinMinerPlayer} data.
 */
public interface PersistentDataStorage {

    /**
     * Get the {@link PersistentStorageType} of this storage.
     *
     * @return the storage type
     */
    @NotNull
    public PersistentStorageType getType();

    /**
     * Initialize this persistent storage.
     *
     * @return a {@link CompletableFuture} completed when the initialization has finished
     */
    @NotNull
    public CompletableFuture<Void> init();

    /**
     * Save the data of the given {@link VeinMinerPlayer} to disk.
     * <p>
     * If the VeinMinerPlayer is not dirty (according to {@link VeinMinerPlayer#isDirty()}), this
     * method will complete immediately and not perform a save.
     *
     * @param player the player to save
     *
     * @return a {@link CompletableFuture} completed when saving has finished
     */
    @NotNull
    public CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlayer player);

    /**
     * Save the data of the given list of {@link VeinMinerPlayer VeinMinerPlayers} to disk.
     *
     * @param players the players to save
     *
     * @return a {@link CompletableFuture} completed when saving has finished
     */
    @NotNull
    public CompletableFuture<List<VeinMinerPlayer>> save(@NotNull Collection<? extends VeinMinerPlayer> players);

    /**
     * Load the data of the given {@link VeinMinerPlayer} from disk.
     *
     * @param player the player whose data to load
     *
     * @return a {@link CompletableFuture} completed when loading has finished
     */
    @NotNull
    public CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlayer player);

    /**
     * Load the data of the given list of {@link VeinMinerPlayer VeinMinerPlayers} from disk.
     *
     * @param players the players whose data to load
     *
     * @return a {@link CompletableFuture} completed when loading has finished
     */
    @NotNull
    public CompletableFuture<List<VeinMinerPlayer>> load(@NotNull Collection<? extends VeinMinerPlayer> players);

}
