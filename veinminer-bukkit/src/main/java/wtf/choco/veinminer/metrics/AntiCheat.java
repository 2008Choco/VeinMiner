package wtf.choco.veinminer.metrics;

import org.jetbrains.annotations.NotNull;

/**
 * Represents primitive information about an anti cheat installed on the server.
 *
 * @param name the name of the anti cheat
 * @param version the version of the anti cheat
 */
public record AntiCheat(@NotNull String name, @NotNull String version) { }
