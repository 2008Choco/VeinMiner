package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemStack {

    @NotNull
    public ItemType getType();

    @Nullable
    public String getVeinMinerNBTValue();

}
