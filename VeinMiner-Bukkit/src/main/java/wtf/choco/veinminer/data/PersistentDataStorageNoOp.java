package wtf.choco.veinminer.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * An implementation of {@link PersistentDataStorage} that performs no save or load
 * operations. This implementation is used only when all other implementations fail.
 */
public final class PersistentDataStorageNoOp implements PersistentDataStorage {

    /**
     * The {@link PersistentDataStorageNoOp} singleton instance.
     */
    public static final PersistentDataStorage INSTANCE = new PersistentDataStorageNoOp();

    private PersistentDataStorageNoOp() { }

    @NotNull
    @Override
    public Type getType() {
        return Type.UNKNOWN;
    }

    @NotNull
    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.completedFuture(null);
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
        return CompletableFuture.completedFuture(player);
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> save(@NotNull VeinMinerPlugin plugin, @NotNull Collection<? extends VeinMinerPlayer> players) {
        return CompletableFuture.completedFuture(new ArrayList<>(players));
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
        return CompletableFuture.completedFuture(player);
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> load(@NotNull VeinMinerPlugin plugin, @NotNull Collection<? extends VeinMinerPlayer> players) {
        return CompletableFuture.completedFuture(new ArrayList<>(players));
    }

}
