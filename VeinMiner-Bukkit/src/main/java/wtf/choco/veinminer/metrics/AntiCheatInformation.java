package wtf.choco.veinminer.metrics;

import com.google.common.base.Preconditions;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an anti cheat installed on the server.
 *
 * @author Parker Hawke - Choco
 */
public final class AntiCheatInformation {

    private final String name;
    private final String version;

    public AntiCheatInformation(@NotNull String name, @NotNull String version) {
        Preconditions.checkArgument(name != null, "name must not be null");
        Preconditions.checkArgument(version != null, "version must not be null");

        this.name = name;
        this.version = version;
    }

    /**
     * Get this anti cheat's name.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get this anti cheat's version.
     *
     * @return the version
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AntiCheatInformation)) {
            return false;
        }

        AntiCheatInformation other = (AntiCheatInformation) obj;
        return Objects.equals(name, other.name) && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return name + " " + version;
    }

}
