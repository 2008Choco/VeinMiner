package wtf.choco.veinminer.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for {@link ItemStack} instances.
 */
public final class ItemStackUtil {

    private ItemStackUtil() { }

    /**
     * Set the String value for VeinMiner's NBT key on the given {@link ItemStack}.
     *
     * @param itemStack the item stack
     * @param value the value to set
     */
    @Nullable
    public static void setVeinMinerNBTValue(@NotNull ItemStack itemStack, @NotNull String value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.getPersistentDataContainer().set(VMConstants.getVeinMinerNBTKey(), PersistentDataType.STRING, value);
        itemStack.setItemMeta(meta);
    }

    /**
     * Get the String value for VeinMiner's NBT key on the given {@link ItemStack}.
     *
     * @param itemStack the item stack
     *
     * @return the value set for VeinMiner's NBT key, or null if not set or if the item
     * does not have any item meta
     */
    @Nullable
    public static String getVeinMinerNBTValue(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();
        return meta != null ? meta.getPersistentDataContainer().get(VMConstants.getVeinMinerNBTKey(), PersistentDataType.STRING) : null;
    }

}
