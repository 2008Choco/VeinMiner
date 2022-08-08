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

    /**
     * Get a {@link NamespacedKey} from an input string. If the input does not have a valid
     * namespace, the provided namespace will be used in conjunction with the rest of the key.
     *
     * @param input the input string
     * @param defaultNamespace the namespace to use if none is present in the input string
     *
     * @return the resulting key
     */
    @NotNull
    public static NamespacedKey fromString(@NotNull String input, @NotNull String defaultNamespace) {
        String[] inputComponents = input.split(":", 2);
        return (inputComponents.length >= 2) ? new NamespacedKey(inputComponents[0], inputComponents[1]) : new NamespacedKey(defaultNamespace, input);
    }

    /**
     * Get a {@link NamespacedKey} from an input string. If the input does not have a valid
     * namespace, the Minecraft namespace will be used in conjunction with the rest of the key.
     *
     * @param input the input string
     *
     * @return the resulting key
     */
    @NotNull
    public static NamespacedKey fromString(@NotNull String input) {
        return fromString(input, NAMESPACE_MINECRAFT);
    }

}
