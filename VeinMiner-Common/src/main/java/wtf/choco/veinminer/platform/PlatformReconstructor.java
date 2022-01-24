package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlatformReconstructor {

    @Nullable
    public BlockState getState(@NotNull String state);

    @Nullable
    public BlockType getBlockType(@NotNull String type);

    @Nullable
    public ItemType getItemType(@NotNull String type);

}
