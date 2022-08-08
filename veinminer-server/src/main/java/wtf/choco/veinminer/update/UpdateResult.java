package wtf.choco.veinminer.update;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a result of an update check from the {@link UpdateChecker}.
 */
public final class UpdateResult {

    private Optional<Throwable> exception = Optional.empty();

    private final String currentVersion, newestVersion;
    private final VersionScheme versionScheme;
    private final boolean updateAvailable;

    /**
     * Construct a new {@link UpdateResult}.
     *
     * @param currentVersion the currently installed version of the plugin
     * @param newestVersion the newest version of the plugin (may or may not equal
     * {@code currentVersion})
     * @param versionScheme the version scheme used in the update check
     * @param updateAvailable whether or not an update is available for download
     */
    public UpdateResult(@NotNull String currentVersion, @NotNull String newestVersion, @NotNull VersionScheme versionScheme, boolean updateAvailable) {
        this.currentVersion = currentVersion;
        this.newestVersion = newestVersion;
        this.versionScheme = versionScheme;
        this.updateAvailable = updateAvailable;
    }

    /**
     * Construct a new {@link UpdateResult}.
     *
     * @param currentVersion the currently installed version of the plugin
     * @param versionScheme the version scheme used in the update check
     * @param exception the exception that was thrown in the update check
     */
    public UpdateResult(@NotNull String currentVersion, @NotNull VersionScheme versionScheme, @NotNull Throwable exception) {
        this(currentVersion, currentVersion, versionScheme, false);
        this.exception = Optional.ofNullable(exception);
    }

    /**
     * Get the currently installed version of the plugin.
     *
     * @return the current version
     */
    @NotNull
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Get the newest available version of the plugin. May or may not equal {@link #getCurrentVersion()}.
     *
     * @return the newest version
     */
    @NotNull
    public String getNewestVersion() {
        return newestVersion;
    }

    /**
     * Get the {@link VersionScheme} used in the update check.
     *
     * @return the version scheme
     */
    @NotNull
    public VersionScheme getVersionScheme() {
        return versionScheme;
    }

    /**
     * Check whether or not there is an update available for download.
     *
     * @return true if an update is available, false if on the latest version
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Check whether or not this update check failed and there was an exception.
     *
     * @return true if the check failed, false if it completed normally
     */
    public boolean isFailed() {
        return exception.isPresent();
    }

    /**
     * Get the {@link Throwable} that was thrown during the update check, if one is present.
     *
     * @return the exception thrown
     */
    @NotNull
    public Optional<Throwable> getException() {
        return exception;
    }

}
