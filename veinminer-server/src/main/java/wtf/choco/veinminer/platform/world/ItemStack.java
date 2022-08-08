package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a stack of items in an inventory.
 */
public interface ItemStack {

    /**
     * Get the {@link ItemType} of this stack.
     *
     * @return the type
     */
    @NotNull
    public ItemType getType();

    /**
     * Get the value of the VeinMiner-defined NBT key for this stack. The returned value
     * acts as a unique identifier for item stacks that require a specific tag.
     *
     * @return the value, or null if not set
     */
    @Nullable
    public String getVeinMinerNBTValue();

}
