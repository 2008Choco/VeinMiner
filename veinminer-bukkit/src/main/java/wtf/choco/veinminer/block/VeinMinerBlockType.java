package wtf.choco.veinminer.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A type of {@link VeinMinerBlock} backed by a {@link Material}.
 */
public final class VeinMinerBlockType implements VeinMinerBlock {

    private final Material type;

    /**
     * Construct a new {@link VeinMinerBlockType}.
     *
     * @param type the type of block
     */
    public VeinMinerBlockType(@NotNull Material type) {
        this.type = type;
    }

    @NotNull
    public Material getType() {
        return type;
    }

    @Override
    public boolean matchesType(@NotNull Material type) {
        return this.type.equals(type);
    }

    @Override
    public boolean matchesState(@NotNull BlockData state, boolean exact) {
        return state != null && state.getMaterial().equals(type);
    }

    @NotNull
    @Override
    public String toStateString() {
        return type.getKey().toString();
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof VeinMinerBlockType other && type.equals(other.type));
    }

    @Override
    public String toString() {
        return String.format("VeinMinerBlockType[type=\"%s\"]", type.getKey().toString());
    }

}
