package wtf.choco.veinminer.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

/**
 * Data storage that can have legacy data imported.
 */
public interface LegacyImportable {

    /**
     * Import legacy data into this database. This makes no attempt at preserving existing entries.
     *
     * @param data the legacy data to import
     *
     * @return a completable future, completed when the import has finished, containing the amount
     * of successfully imported entries
     */
    @NotNull
    public CompletableFuture<Integer> importLegacyData(@NotNull List<LegacyPlayerData> data);

}
