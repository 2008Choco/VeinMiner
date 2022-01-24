package wtf.choco.veinminer.block;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.BlockState;
import wtf.choco.veinminer.platform.BlockType;

/**
 * A type of {@link VeinMinerBlock} backed by a {@link BlockType}.
 */
public final class VeinMinerBlockType implements VeinMinerBlock {

    private final BlockType type;
    private final BlockState defaultBlockState;

    /**
     * Construct a new {@link VeinMinerBlockType}.
     *
     * @param type the type of block
     */
    public VeinMinerBlockType(@NotNull BlockType type) {
        this.type = type;
        this.defaultBlockState = type.createBlockState("[]");
    }

    @NotNull
    @Override
    public BlockType getType() {
        return type;
    }

    @NotNull
    @Override
    public BlockState getState() {
        return defaultBlockState;
    }

    @Override
    public boolean hasState() {
        return false;
    }

    @Override
    public boolean matchesType(@NotNull BlockType type) {
        return this.type.equals(type);
    }

    @Override
    public boolean matchesState(@NotNull BlockState state, boolean exact) {
        return state != null && state.getType().equals(type);
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
