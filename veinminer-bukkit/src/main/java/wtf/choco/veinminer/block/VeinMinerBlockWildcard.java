package wtf.choco.veinminer.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A type of {@link VeinMinerBlock} that passes all states and types.
 */
public final class VeinMinerBlockWildcard implements VeinMinerBlock {

    VeinMinerBlockWildcard() { }

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
