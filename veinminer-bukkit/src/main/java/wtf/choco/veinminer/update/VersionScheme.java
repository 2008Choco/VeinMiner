package wtf.choco.veinminer.update;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface to compare two version Strings under the same format.
 */
@FunctionalInterface
public interface VersionScheme {

    /**
     * Compare two version strings.
     *
     * @param versionA the first version to compare
     * @param versionB the second version to compare
     *
     * @return -1 if {@code versionA} is less than {@code versionB}, 1 if {@code versionA}
     * is greater than {@code versionB}, or 0 if the two versions are equivalent
     *
     * @throws UnsupportedOperationException if either of the two versions do not abide
     * by this version scheme's expected format
     */
    public int compareVersions(@NotNull String versionA, @NotNull String versionB) throws UnsupportedOperationException;

}
