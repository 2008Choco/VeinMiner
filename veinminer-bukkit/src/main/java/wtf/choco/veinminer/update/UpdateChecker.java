package wtf.choco.veinminer.update;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple update checker.
 */
public interface UpdateChecker {

    /**
     * Get the {@link UpdateResult UpdateResult} of the last update check that was performed.
     *
     * @return the last update result, or an empty {@link Optional} if an update check has
     * not yet been performed
     */
    @NotNull
    public Optional<UpdateResult> getLastUpdateResult();

    /**
     * Perform an asynchronous update check.
     *
     * @param versionScheme the {@link VersionScheme} to use
     *
     * @return a {@link CompletableFuture} containing the {@link UpdateResult}
     */
    @NotNull
    public CompletableFuture<UpdateResult> checkForUpdates(@NotNull VersionScheme versionScheme);

}
