package wtf.choco.veinminer.utils;

import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class to better handle {@link NamespacedKey} objects.
 *
 * @author Parker Hawke - Choco
 */
public final class NamespacedKeyUtil {

    /* NOTE:
     *   #fromString() was now successfully merged into Bukkit 1.16.5. However, because
     *   of its minimal use in VeinMiner and the ideal situation being to continue to
     *   support 1.13.x+ for as long as possible, continuing to use this as a utility class
     *   as I have now for a year or two is sufficient.
     */

    // Pulled from Bukkit's NamespacedKey
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    private NamespacedKeyUtil() { }

    /**
     * Get a NamespacedKey from the supplied string with a default namespace if
     * a namespace is not defined. This is a utility method meant to fetch a
     * NamespacedKey from user input. Please note that casing does matter and
     * any instance of uppercase characters will be considered invalid. The
     * input contract is as follows:
     * <pre>
     * fromString("foo", plugin) -{@literal >} "plugin:foo"
     * fromString("foo:bar", plugin) -{@literal >} "foo:bar"
     * fromString(":foo", null) -{@literal >} "minecraft:foo"
     * fromString("foo", null) -{@literal >} "minecraft:foo"
     * fromString("Foo", plugin) -{@literal >} null
     * fromString(":Foo", plugin) -{@literal >} null
     * fromString("foo:bar:bazz", plugin) -{@literal >} null
     * fromString("", plugin) -{@literal >} null
     * </pre>
     *
     * @param string the string to convert to a NamespacedKey
     * @param defaultNamespace the default namespace to use if none was
     * supplied. If null, the {@code minecraft} namespace
     * ({@link NamespacedKey#minecraft(String)}) will be used
     *
     * @return the created NamespacedKey. null if invalid key
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public static NamespacedKey fromString(@NotNull String string, @Nullable Plugin defaultNamespace) {
        Preconditions.checkArgument(string != null && !string.isEmpty(), "Input string must not be empty or null");

        String[] components = string.split(":", 3);
        if (components.length > 2) {
            return null;
        }

        String key = (components.length == 2) ? components[1] : "";
        if (components.length == 1) {
            String value = components[0];
            if (value.isEmpty() || !VALID_KEY.matcher(value).matches()) {
                return null;
            }

            return (defaultNamespace != null) ? new NamespacedKey(defaultNamespace, value) : NamespacedKey.minecraft(value);
        } else if (components.length == 2 && !VALID_KEY.matcher(key).matches()) {
            return null;
        }

        String namespace = components[0];
        if (namespace.isEmpty()) {
            return (defaultNamespace != null) ? new NamespacedKey(defaultNamespace, key) : NamespacedKey.minecraft(key);
        }

        if (!VALID_KEY.matcher(namespace).matches()) {
            return null;
        }

        return new NamespacedKey(namespace, key);
    }

    /**
     * Check whether or not the provided string is a valid key for a
     * {@link NamespacedKey}.
     *
     * @param string the string to check
     *
     * @return true if valid, false otherwise
     */
    public static boolean isValidKey(@NotNull String string) {
        return string != null && VALID_KEY.matcher(string).matches();
    }

}
