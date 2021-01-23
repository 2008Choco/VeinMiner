package wtf.choco.veinminer.data.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

final class VeinBlockWildcard implements VeinBlock {

    static final VeinBlock INSTANCE = new VeinBlockWildcard();

    private VeinBlockWildcard() { }

    @Override
    @NotNull
    public Material getType() {
        return Material.AIR;
    }

    @Override
    public boolean hasSpecificData() {
        return false;
    }

    @Override
    @NotNull
    public BlockData getBlockData() {
        return Material.AIR.createBlockData();
    }

    @Override
    public boolean encapsulates(@NotNull Block block) {
        return true;
    }

    @Override
    public boolean encapsulates(@NotNull BlockData data) {
        return true;
    }

    @Override
    public boolean encapsulates(@NotNull Material material) {
        return true;
    }

    @Override
    @NotNull
    public String asDataString() {
        return "*";
    }

    @Override
    public boolean isWildcard() {
        return true;
    }

}
