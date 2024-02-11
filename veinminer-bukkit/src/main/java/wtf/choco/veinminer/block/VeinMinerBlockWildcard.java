package wtf.choco.veinminer.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A type of {@link VeinMinerBlock} that passes all states and types.
 */
public final class VeinMinerBlockWildcard implements VeinMinerBlock {

    VeinMinerBlockWildcard() { }

    @NotNull
    @Override
    public Material getType() {
        return Material.AIR;
    }

    @NotNull
    @Override
    public BlockData getState() {
        return Material.AIR.createBlockData();
    }

    @Override
    public boolean hasState() {
        return false;
    }

    @Override
    public boolean matchesType(@NotNull Material type) {
        return true;
    }

    @Override
    public boolean matchesState(@NotNull BlockData state, boolean exact) {
        return true;
    }

    @NotNull
    @Override
    public String toStateString() {
        return "*";
    }

    @Override
    public String toString() {
        return "VeinMinerBlockWildcard";
    }

}
