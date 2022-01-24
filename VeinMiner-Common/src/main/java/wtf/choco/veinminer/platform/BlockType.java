package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

public interface BlockType {

    @NotNull
    public NamespacedKey getKey();

    @NotNull
    public BlockState createBlockState(@NotNull String states);

}
