package wtf.choco.veinminer.block;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;

/**
 * A type of {@link VeinMinerBlock} backed by a {@link BlockState}.
 */
public final class VeinMinerBlockState implements VeinMinerBlock {

    private final BlockState state;

    /**
     * Construct a new {@link VeinMinerBlockState}.
     *
     * @param state the state
     */
    public VeinMinerBlockState(@NotNull BlockState state) {
        this.state = state;
    }

    @NotNull
    @Override
    public BlockType getType() {
        return state.getType();
    }

    @NotNull
    @Override
    public BlockState getState() {
        return state;
    }

    @Override
    public boolean hasState() {
        return true;
    }

    @Override
    public boolean matchesType(@NotNull BlockType type) {
        return false; // Will never match a general type. State always has AT LEAST one state
    }

    @Override
    public boolean matchesState(@NotNull BlockState state, boolean exact) {
        return state != null && (exact ? state.equals(this.state) : state.matches(this.state));
    }

    @NotNull
    @Override
    public String toStateString() {
        return state.getAsString(true);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof VeinMinerBlockState other && Objects.equals(state, other.state));
    }

    @Override
    public String toString() {
        return String.format("VeinMinerBlockState[state=\"%s\"]", state.getAsString(true));
    }

}
