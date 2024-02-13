package wtf.choco.veinminer.pattern;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A registry to which {@link VeinMiningPattern VeinMiningPatterns} may be registered.
 */
public final class PatternRegistry {

    private final Map<NamespacedKey, VeinMiningPattern> patterns = new HashMap<>();

    /**
     * Register a new {@link VeinMiningPattern}.
     *
     * @param pattern the pattern to register
     */
    public void register(@NotNull VeinMiningPattern pattern) {
        this.patterns.put(pattern.getKey(), pattern);
    }

    /**
     * Get the pattern with the given {@link NamespacedKey}.
     *
     * @param key the pattern key
     *
     * @return the pattern
     */
    @Nullable
    public VeinMiningPattern get(@NotNull NamespacedKey key) {
        return patterns.get(key);
    }

    /**
     * Get the pattern with the given {@link NamespacedKey}, or return a default pattern if
     * a pattern with the given key does not exist.
     *
     * @param key the pattern key
     * @param defaultPattern the default pattern to return if unavailable
     *
     * @return the pattern, or the default pattern if unavailable
     */
    @NotNull
    public VeinMiningPattern getOrDefault(@NotNull NamespacedKey key, @NotNull VeinMiningPattern defaultPattern) {
        return patterns.getOrDefault(key, defaultPattern);
    }

    /**
     * Get the pattern with the given String-representation of a {@link NamespacedKey}.
     *
     * @param key the pattern key
     *
     * @return the pattern
     */
    @Nullable
    public VeinMiningPattern get(@NotNull String key) {
        for (Entry<NamespacedKey, VeinMiningPattern> entry : patterns.entrySet()) {
            if (entry.getKey().toString().equals(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Get the pattern with the given String-representation of a {@link NamespacedKey}, or
     * return a default pattern if a pattern with the given key does not exist.
     *
     * @param key the pattern key
     * @param defaultPattern the default pattern to return if unavailable
     *
     * @return the pattern, or the default pattern if unavailable
     */
    @NotNull
    public VeinMiningPattern getOrDefault(@NotNull String key, @NotNull VeinMiningPattern defaultPattern) {
        for (Entry<NamespacedKey, VeinMiningPattern> entry : patterns.entrySet()) {
            if (entry.getKey().toString().equals(key)) {
                return entry.getValue();
            }
        }

        return defaultPattern;
    }

    /**
     * Unregister the given {@link VeinMiningPattern}.
     *
     * @param pattern the pattern to unregister
     *
     * @return true if unregistered, false if the pattern was not previously registered
     */
    public boolean unregister(@NotNull VeinMiningPattern pattern) {
        return (unregister(pattern.getKey()) != null);
    }

    /**
     * Unregister the pattern with the given {@link NamespacedKey}.
     *
     * @param key the key of the pattern to unregister
     *
     * @return the unregistered {@link VeinMiningPattern}, or null if none unregistered
     */
    @Nullable
    public VeinMiningPattern unregister(@NotNull NamespacedKey key) {
        return patterns.remove(key);
    }

    /**
     * Get an unmodifiable {@link Collection} of all {@link VeinMiningPattern VeinMiningPatterns}
     * in this registry.
     *
     * @return all registered patterns
     */
    @NotNull
    public Collection<VeinMiningPattern> getPatterns() {
        return Collections.unmodifiableCollection(patterns.values());
    }

    /**
     * Unregister all patterns in this registry.
     */
    public void unregisterAll() {
        this.patterns.clear();
    }

}
