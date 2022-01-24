package wtf.choco.veinminer.util;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a unique key with a namespace. Keys are under the format {@code namespace:key}.
 *
 * @param namespace the namespace
 * @param key the key
 */
public record NamespacedKey(@NotNull String namespace, @NotNull String key) {

    private static final String NAMESPACE_MINECRAFT = "minecraft";
    private static final String NAMESPACE_VEINMINER = "veinminer";

    @Override
    public String toString() {
        return namespace + ":" + key;
    }

    /**
     * Get a {@link NamespacedKey} under the {@code minecraft} namespace.
     *
     * @param key the key
     *
     * @return the Minecraft namespaced key
     */
    @NotNull
    public static NamespacedKey minecraft(@NotNull String key) {
        return new NamespacedKey(NAMESPACE_MINECRAFT, key);
    }

    /**
     * Get a {@link NamespacedKey} under the {@code veinminer} namespace.
     *
     * @param key the key
     *
     * @return the VeinMiner namespaced key
     */
    @NotNull
    public static NamespacedKey veinminer(@NotNull String key) {
        return new NamespacedKey(NAMESPACE_VEINMINER, key);
    }

}
