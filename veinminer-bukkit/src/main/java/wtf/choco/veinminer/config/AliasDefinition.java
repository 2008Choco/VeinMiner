package wtf.choco.veinminer.config;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public record AliasDefinition(@NotNull String key, @NotNull List<String> entries) { }
