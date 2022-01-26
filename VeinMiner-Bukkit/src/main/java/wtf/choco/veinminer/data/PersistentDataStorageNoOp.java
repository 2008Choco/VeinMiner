package wtf.choco.veinminer.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;

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
    public CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
        return CompletableFuture.completedFuture(player);
    }

    @Override
    public void close() { }

}
