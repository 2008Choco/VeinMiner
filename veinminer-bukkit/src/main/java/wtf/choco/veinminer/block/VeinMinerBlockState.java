package wtf.choco.veinminer.block;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A type of {@link VeinMinerBlock} backed by a {@link BlockData}.
 */
public final class VeinMinerBlockState implements VeinMinerBlock {

    private final BlockData state;

    /**
     * Construct a new {@link VeinMinerBlockState}.
     *
     * @param state the state
     */
    public VeinMinerBlockState(@NotNull BlockData state) {
        this.state = state;
    }

    @NotNull
    public BlockData getState() {
        return state.clone();
    }

    @Override
    public boolean isTangible() {
        return true;
    }

    @Override
    public boolean matchesType(@NotNull Material type) {
        return false; // Will never match a general type. State always has AT LEAST one state
    }

    @Override
    public boolean matchesState(@NotNull BlockData state, boolean exact) {
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
