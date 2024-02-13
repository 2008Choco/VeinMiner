package wtf.choco.veinminer.network;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class to convert between common Networking and Bukkit types.
 */
public final class NetworkUtil {

    private NetworkUtil() { }

    /**
     * Convert a Bukkit {@link NamespacedKey} to a Networking {@link wtf.choco.network.data.NamespacedKey}.
     *
     * @param key the key to convert
     *
     * @return the converted key
     */
    @NotNull
    public static wtf.choco.network.data.NamespacedKey toNetwork(@NotNull NamespacedKey key) {
        return wtf.choco.network.data.NamespacedKey.of(key.getNamespace(), key.getKey());
    }

    /**
     * Convert a Networking {@link wtf.choco.network.data.NamespacedKey} to a Bukkit {@link NamespacedKey}.
     *
     * @param key the key to convert
     *
     * @return the converted key
     */
    @NotNull
    public static NamespacedKey toBukkit(@NotNull wtf.choco.network.data.NamespacedKey key) {
        NamespacedKey bukkitKey = NamespacedKey.fromString(key.toString());
        if (bukkitKey == null) {
            throw new IllegalArgumentException("Invalid key: \"" + key + "\". Could not convert to Bukkit NamespacedKey");
        }

        return bukkitKey;
    }

    /**
     * Convert a {@link Collection} of Bukkit {@link NamespacedKey NamespacedKeys} to a Collection of
     * Networking {@link wtf.choco.network.data.NamespacedKey NamespacedKeys}.
     *
     * @param keys the collection of keys to convert
     *
     * @return the converted keys
     */
    @NotNull
    public static Collection<wtf.choco.network.data.NamespacedKey> toNetwork(@NotNull Collection<NamespacedKey> keys) {
        return Collections2.transform(keys, NetworkUtil::toNetwork);
    }

    /**
     * Convert a {@link Collection} of Networking {@link wtf.choco.network.data.NamespacedKey NamespacedKeys}
     * to a Collection of Bukkit {@link NamespacedKey NamespacedKeys}.
     *
     * @param keys the collection of keys to convert
     *
     * @return the converted keys
     */
    @NotNull
    public static Collection<NamespacedKey> toBukkit(@NotNull Collection<wtf.choco.network.data.NamespacedKey> keys) {
        return Collections2.transform(keys, NetworkUtil::toBukkit);
    }

    /**
     * Convert a {@link List} of Bukkit {@link NamespacedKey NamespacedKeys} to a List of Networking
     * {@link wtf.choco.network.data.NamespacedKey NamespacedKeys}.
     *
     * @param keys the list of keys to convert
     *
     * @return the converted keys
     */
    @NotNull
    public static List<wtf.choco.network.data.NamespacedKey> toNetwork(@NotNull List<NamespacedKey> keys) {
        return Lists.transform(keys, NetworkUtil::toNetwork);
    }

    /**
     * Convert a {@link List} of Networking {@link wtf.choco.network.data.NamespacedKey NamespacedKeys}
     * to a List of Bukkit {@link NamespacedKey NamespacedKeys}.
     *
     * @param keys the list of keys to convert
     *
     * @return the converted keys
     */
    @NotNull
    public static List<NamespacedKey> toBukkit(@NotNull List<wtf.choco.network.data.NamespacedKey> keys) {
        return Lists.transform(keys, NetworkUtil::toBukkit);
    }

}
