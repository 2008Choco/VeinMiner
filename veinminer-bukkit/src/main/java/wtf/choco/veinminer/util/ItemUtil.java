package wtf.choco.veinminer.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemUtil {

    private ItemUtil() { }

    @Nullable
    public static String getVeinMinerNBTValue(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();
        return meta != null ? meta.getPersistentDataContainer().get(VMConstants.getVeinMinerNBTKey(), PersistentDataType.STRING) : null;
    }

}
