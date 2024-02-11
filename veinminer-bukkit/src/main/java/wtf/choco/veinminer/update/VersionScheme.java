package wtf.choco.veinminer.update;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface to compare two version Strings with similar version schemes.
 */
@FunctionalInterface
public interface VersionScheme {

    /**
     * Compare two versions and return the higher of the two.
     *
     * @param first the first version to check
     * @param second the second version to check
     *
     * @return the greater of the two versions
     *
     * @throws UnsupportedOperationException if either of the two versions do not abide
     * by this version scheme's expected format
     */
    @NotNull
    public String compareVersions(@NotNull String first, @NotNull String second) throws UnsupportedOperationException;

}
