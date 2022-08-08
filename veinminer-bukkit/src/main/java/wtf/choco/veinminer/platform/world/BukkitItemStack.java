package wtf.choco.veinminer.platform.world;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.util.VMConstants;

/**
 * A Bukkit implementation of {@link ItemStack}.
 */
public final class BukkitItemStack implements ItemStack {

    private final org.bukkit.inventory.ItemStack itemStack;

    /**
     * Construct a new {@link BukkitItemStack}.
     *
     * @param itemStack the bukkit item stack to wrap
     */
    public BukkitItemStack(org.bukkit.inventory.ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @NotNull
    @Override
    public ItemType getType() {
        return BukkitItemType.of(itemStack.getType());
    }

    @Nullable
    @Override
    public String getVeinMinerNBTValue() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null ? meta.getPersistentDataContainer().get(VMConstants.getVeinMinerNBTKey(), PersistentDataType.STRING) : null;
    }

}
