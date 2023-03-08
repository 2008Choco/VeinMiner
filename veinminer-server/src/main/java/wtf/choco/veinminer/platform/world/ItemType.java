package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * Represents a type of item.
 */
public interface ItemType {

    /**
     * Get the {@link NamespacedKey} of this {@link ItemType}.
     *
     * @return the key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Check whether or not this item is air.
     *
     * @return true if air, false otherwise
     */
    public boolean isAir();

}
