package wtf.choco.veinminer.client;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;

/**
 * A class handling the logic for when a player moves from tile to tile, requesting the
 * server to update the vein mining for the new position (if necesary).
 */
public final class BlockLookUpdateHandler {

    private BlockPos lastLookedAtBlockPos;
    private Direction lastLookedAtBlockFace;
    private BlockState lastLookedAtBlockState;

    private final VeinMinerClient client;

    BlockLookUpdateHandler(@NotNull VeinMinerClient client) {
        this.client = client;
    }

    /**
     * Update on the client the last looked position with the current looking position,
     * and send a request to the server to update the wireframe if necessary.
     *
     * @param minecraft the minecraft instance
     */
    public void tick(@NotNull Minecraft minecraft) {
        if (!client.hasServerState()) {
            return;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }

        FabricServerState serverState = client.getServerState();
        BlockPos lookingAtPos = hit.getBlockPos();
        Direction lookingAtFace = hit.getDirection();
        BlockState lookingAtState = minecraft.level.getBlockState(lookingAtPos);

        this.updateWireframeIfNecessary(serverState, lookingAtPos, lookingAtFace, lookingAtState);

        // Updating the new last looked at position
        if (minecraft.player != null && minecraft.player.level() != null && !minecraft.player.level().isEmptyBlock(lookingAtPos)) {
            this.lastLookedAtBlockPos = lookingAtPos;
            this.lastLookedAtBlockFace = lookingAtFace;
            this.lastLookedAtBlockState = lookingAtState;
        } else {
            this.reset();
        }
    }

    /**
     * Reset the last looked at state data.
     */
    public void reset() {
        this.lastLookedAtBlockPos = null;
        this.lastLookedAtBlockFace = null;
        this.lastLookedAtBlockState = null;
    }

    /**
     * Get the last looked at {@link BlockPos}.
     *
     * @return the last looked at block pos
     */
    @Nullable
    public BlockPos getLastLookedAtBlockPos() {
        return lastLookedAtBlockPos;
    }

    private void updateWireframeIfNecessary(@NotNull FabricServerState serverState, @NotNull BlockPos lookingAtPos, @NotNull Direction lookingAtFace, @NotNull BlockState lookingAtState) {
        ClientConfig config = serverState.getConfig();
        if (!serverState.isActive() || !config.isAllowActivationKeybind()) {
            return;
        }

        if (isLookingAtDifferentPositionOrState(lookingAtPos, lookingAtFace, lookingAtState)) {
            serverState.resetShape();
            serverState.sendMessage(new ServerboundRequestVeinMine(lookingAtPos.getX(), lookingAtPos.getY(), lookingAtPos.getZ()));
        }
    }

    private boolean isLookingAtDifferentPositionOrState(@NotNull BlockPos lookingAtPos, @NotNull Direction lookingAtFace, @NotNull BlockState lookingAtState) {
        return !Objects.equals(lastLookedAtBlockPos, lookingAtPos)
                || !Objects.equals(lastLookedAtBlockFace, lookingAtFace)
                || !Objects.equals(lastLookedAtBlockState, lookingAtState);
    }

}
