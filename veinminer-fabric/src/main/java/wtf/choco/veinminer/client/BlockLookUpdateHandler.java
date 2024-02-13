package wtf.choco.veinminer.client;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;

/**
 * A class handling the logic for when a player moves from tile to tile, requesting the
 * server to update the vein mining for the new position (if necesary).
 */
public final class BlockLookUpdateHandler {

    private final VeinMinerClient client;

    BlockLookUpdateHandler(@NotNull VeinMinerClient client) {
        this.client = client;
    }

    /**
     * Update on the client the last looked position with the current looking position,
     * and send a request to the server to update the wireframe if necessary.
     *
     * @param minecraft
     */
    public void updateLastLookedPosition(@NotNull Minecraft minecraft) {
        if (!client.hasServerState()) {
            return;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }

        FabricServerState serverState = client.getServerState();
        BlockPos position = hit.getBlockPos();
        Direction blockFace = hit.getDirection();

        this.updateWireframeIfNecessary(serverState, position, blockFace);

        // Updating the new last looked at position
        if (minecraft.player != null && minecraft.player.level() != null && !minecraft.player.level().isEmptyBlock(position)) {
            serverState.setLastLookedAt(position, blockFace);
        } else {
            serverState.setLastLookedAt(null, null);
        }
    }

    private void updateWireframeIfNecessary(@NotNull FabricServerState serverState, @NotNull BlockPos lookingAtPos, @NotNull Direction lookingAtFace) {
        ClientConfig config = serverState.getConfig();
        if (!serverState.isActive() || !config.isAllowActivationKeybind()) {
            return;
        }

        if (isLookingAtDifferentPosition(serverState, lookingAtPos, lookingAtFace)) {
            serverState.resetShape();
            serverState.sendMessage(new ServerboundRequestVeinMine(lookingAtPos.getX(), lookingAtPos.getY(), lookingAtPos.getZ()));
        }
    }

    private boolean isLookingAtDifferentPosition(@NotNull FabricServerState serverState, @NotNull BlockPos lookingAtPos, @NotNull Direction lookingAtFace) {
        return !Objects.equals(serverState.getLastLookedAtBlockPos(), lookingAtPos) || !Objects.equals(serverState.getLastLookedAtBlockFace(), lookingAtFace);
    }

}
