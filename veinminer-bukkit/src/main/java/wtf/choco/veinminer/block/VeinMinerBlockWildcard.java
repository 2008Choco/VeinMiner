package wtf.choco.veinminer.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type of {@link VeinMinerBlock} that passes all states and types.
 */
final class VeinMinerBlockWildcard implements VeinMinerBlock {

    static final VeinMinerBlockWildcard INSTANCE = new VeinMinerBlockWildcard();

    VeinMinerBlockWildcard() { }

    @Override
    public boolean isTangible() {
        return true;
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
    public int compareTo(@Nullable VeinMinerBlock other) {
        return -1; // Always at the top
    }

    @Override
    public String toString() {
        return "VeinMinerBlockWildcard";
    }

}
