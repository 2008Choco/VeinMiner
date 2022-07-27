package wtf.choco.veinminer.block;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;

/**
 * A type of {@link VeinMinerBlock} that passes all states and types.
 */
public final class VeinMinerBlockWildcard implements VeinMinerBlock {

    private BlockType air;
    private BlockState defaultBlockState;

    VeinMinerBlockWildcard() { }

    @NotNull
    @Override
    public BlockType getType() {
        if (air == null) {
            this.air = VeinMinerServer.getInstance().getPlatform().getBlockType("minecraft:air");
        }

        return air;
    }

    @NotNull
    @Override
    public BlockState getState() {
        if (defaultBlockState == null) {
            this.defaultBlockState = getType().createBlockState("[]");
        }

        return defaultBlockState;
    }

    @Override
    public boolean hasState() {
        return false;
    }

    @Override
    public boolean matchesType(@NotNull BlockType type) {
        return true;
    }

    @Override
    public boolean matchesState(@NotNull BlockState state, boolean exact) {
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
