package wtf.choco.veinminer.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AliasDefinition(@NotNull String key, @NotNull List<String> entries) { }
