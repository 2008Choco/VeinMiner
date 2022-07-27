package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.util.BlockFace;
import wtf.choco.veinminer.util.BlockPosition;

public final class RayTraceResult {

    private final BlockPosition hitBlock;
    private final BlockFace hitBlockFace;

    public RayTraceResult(@Nullable BlockPosition hitBlock, @Nullable BlockFace hitBlockFace) {
        this.hitBlock = hitBlock;
        this.hitBlockFace = hitBlockFace;
    }

    public RayTraceResult() {
        this(null, null);
    }

    public boolean isHit() {
        return hitBlock != null;
    }

    @Nullable
    public BlockPosition getHitBlock() {
        return hitBlock;
    }

    @Nullable
    public BlockFace getHitBlockFace() {
        return hitBlockFace;
    }

}
