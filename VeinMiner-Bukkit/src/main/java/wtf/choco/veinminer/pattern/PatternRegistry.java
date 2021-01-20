package wtf.choco.veinminer.pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A registry to register any custom implementations of {@link VeinMiningPattern}.
 */
public class PatternRegistry {

    private final Map<@NotNull NamespacedKey, @NotNull VeinMiningPattern> patterns = new HashMap<>();

    /**
     * Register a new VeinMiningPattern implementation.
     *
     * @param pattern the pattern to register
     */
    public void registerPattern(@NotNull VeinMiningPattern pattern) {
        Preconditions.checkNotNull(pattern, "Cannot register a null pattern");
        Preconditions.checkNotNull(pattern.getKey(), "Vein mining patterns must have a non-null key");
        Preconditions.checkArgument(!patterns.containsKey(pattern.getKey()), "Patterns must have a unique key (%s)", pattern.getKey().toString());

        this.patterns.put(pattern.getKey(), pattern);
    }

    /**
     * Get the pattern associated with the given key.
     *
     * @param key the key of the pattern to retrieve
     *
     * @return the pattern. null if no pattern matches the given key
     */
    @Nullable
    public VeinMiningPattern getPattern(@NotNull NamespacedKey key) {
        return patterns.get(key);
    }

    /**
     * Get the pattern associated with the given key or default if one is not registered.
     *
     * @param key the key of the pattern to retrieve
     * @param defaultPattern the default pattern in the case the key is not registered
     *
     * @return the pattern. The default pattern if no pattern matches the given key
     */
    @NotNull
    public VeinMiningPattern getPatternOrDefault(@NotNull NamespacedKey key, @NotNull VeinMiningPattern defaultPattern) {
        return patterns.getOrDefault(key, defaultPattern);
    }

    /**
     * Get a pattern associated with the given key in the form of a String.
     *
     * @param key the key of the pattern to retrieve
     *
     * @return the pattern. null if no pattern matches the given key
     */
    @Nullable
    public VeinMiningPattern getPattern(@NotNull String key) {
        for (Entry<@NotNull NamespacedKey, @NotNull VeinMiningPattern> entry : patterns.entrySet()) {
            if (entry.getKey().toString().equals(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Get the pattern associated with the given key in the form of a String, or default if one is
     * not registered.
     *
     * @param key the key of the pattern to retrieve
     * @param defaultPattern the default pattern in the case the key is not registered
     *
     * @return the pattern. The default pattern if no pattern matches the given key
     */
    @NotNull
    public VeinMiningPattern getPatternOrDefault(@NotNull String key, @NotNull VeinMiningPattern defaultPattern) {
        for (Entry<@NotNull NamespacedKey, @NotNull VeinMiningPattern> entry : patterns.entrySet()) {
            if (entry.getKey().toString().equals(key)) {
                return entry.getValue();
            }
        }

        return defaultPattern;
    }

    /**
     * Unregister the provided pattern from the pattern registry.
     *
     * @param pattern the pattern to unregister
     */
    public void unregisterPattern(@NotNull VeinMiningPattern pattern) {
        this.patterns.remove(pattern.getKey());
    }

    /**
     * Unregister the pattern associated with the given key from the pattern registry.
     *
     * @param key the key of the pattern to unregister
     */
    public void unregisterPattern(@NotNull NamespacedKey key) {
        this.patterns.remove(key);
    }

    /**
     * Get an immutable set of all registered patterns.
     *
     * @return all registered patterns
     */
    @NotNull
    public Set<@NotNull VeinMiningPattern> getPatterns() {
        return ImmutableSet.copyOf(patterns.values());
    }

    /**
     * Clear all patterns from the registry.
     */
    public void clearPatterns() {
        this.patterns.clear();
    }

}
