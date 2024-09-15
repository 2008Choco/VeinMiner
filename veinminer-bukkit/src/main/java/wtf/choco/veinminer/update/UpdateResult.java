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
    private final boolean updateAvailable, unreleased;

    private UpdateResult(@NotNull String currentVersion, @NotNull String newestVersion, @NotNull VersionScheme versionScheme, boolean updateAvailable, boolean unreleased) {
        this.currentVersion = currentVersion;
        this.newestVersion = newestVersion;
        this.versionScheme = versionScheme;
        this.updateAvailable = updateAvailable;
        this.unreleased = unreleased;
    }

    private UpdateResult(@NotNull String currentVersion, @NotNull VersionScheme versionScheme, @NotNull Throwable exception) {
        this(currentVersion, currentVersion, versionScheme, false, false);
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
     * Check whether or not the current version is unreleased (i.e. the current version is newer
     * than the one publicly available).
     *
     * @return true if unreleased, false otherwise
     */
    public boolean isUnreleased() {
        return unreleased;
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

    /**
     * Create an {@link UpdateResult} identifying that an update is available.
     *
     * @param currentVersion the currently installed version
     * @param latestVersion the latest version available for download
     * @param versionScheme the version scheme used to compare versions
     *
     * @return the update result
     */
    @NotNull
    static UpdateResult updateAvailable(@NotNull String currentVersion, @NotNull String latestVersion, @NotNull VersionScheme versionScheme) {
        return new UpdateResult(currentVersion, latestVersion, versionScheme, true, false);
    }

    /**
     * Create an {@link UpdateResult} identifying that no update is available, the user is up to date.
     *
     * @param currentVersion the currently installed version
     * @param versionScheme the version scheme used to compare versions
     *
     * @return the update result
     */
    @NotNull
    static UpdateResult upToDate(@NotNull String currentVersion, @NotNull VersionScheme versionScheme) {
        return new UpdateResult(currentVersion, currentVersion, versionScheme, false, false);
    }

    /**
     * Create an {@link UpdateResult} identifying that the installed version is unreleased and is likely
     * a development build.
     *
     * @param currentVersion the currently installed version
     * @param latestVersion the latest version available for download
     * @param versionScheme the version scheme used to compare versions
     *
     * @return the update result
     */
    @NotNull
    static UpdateResult unreleased(@NotNull String currentVersion, @NotNull String latestVersion, @NotNull VersionScheme versionScheme) {
        return new UpdateResult(currentVersion, latestVersion, versionScheme, false, true);
    }

    /**
     * Create an {@link UpdateResult} identifying that the update check failed for some reason.
     *
     * @param currentVersion the currently installed version
     * @param versionScheme the version scheme used to compare versions
     * @param exception the exception that was raised while performing the update check
     *
     * @return the update result
     */
    @NotNull
    static UpdateResult failed(@NotNull String currentVersion, @NotNull VersionScheme versionScheme, @NotNull Throwable exception) {
        return new UpdateResult(currentVersion, versionScheme, exception);
    }

}
