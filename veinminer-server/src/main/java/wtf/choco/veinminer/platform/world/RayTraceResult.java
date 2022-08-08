package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.platform.PlatformPlayer;
import wtf.choco.veinminer.util.BlockFace;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * The result of a ray trace.
 *
 * @see PlatformPlayer#getTargetBlock(int)
 */
public final class RayTraceResult {

    private final BlockPosition hitBlock;
    private final BlockFace hitBlockFace;

    /**
     * Construct a new {@link RayTraceResult}.
     *
     * @param hitBlock the block position that was hit
     * @param hitBlockFace the face on which the ray trace landed
     */
    public RayTraceResult(@Nullable BlockPosition hitBlock, @Nullable BlockFace hitBlockFace) {
        this.hitBlock = hitBlock;
        this.hitBlockFace = hitBlockFace;
    }

    /**
     * Construct an empty {@link RayTraceResult}.
     */
    public RayTraceResult() {
        this(null, null);
    }

    /**
     * Check whether or not there was a successful hit.
     *
     * @return true if hit, false if all values in this result are null
     */
    public boolean isHit() {
        return hitBlock != null;
    }

    /**
     * Get the {@link BlockPosition} that was hit in this result, or null if this result does
     * not represent a {@link #isHit() hit}.
     *
     * @return the hit block, or null
     */
    @Nullable
    public BlockPosition getHitBlock() {
        return hitBlock;
    }

    /**
     * Get the {@link BlockFace} that was hit in this result, or null if this result does not
     * represent a {@link #isHit() hit}.
     *
     * @return the hit block face, or null
     */
    @Nullable
    public BlockFace getHitBlockFace() {
        return hitBlockFace;
    }

}
