package wtf.choco.veinminer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

/**
 * A renderer for wireframed voxel shapes.
 */
public final class WireframeShapeRenderer {

    private final VeinMinerClient client;

    /**
     * Construct a new {@link WireframeShapeRenderer}.
     *
     * @param client the client instance
     */
    public WireframeShapeRenderer(@NotNull VeinMinerClient client) {
        this.client = client;
    }

    /**
     * Render the wireframe of the shape currently stored in the client's server state.
     *
     * @param context the world render context
     */
    public void render(@NotNull WorldRenderContext context) {
        if (!client.hasServerState()) {
            return;
        }

        FabricServerState serverState = client.getServerState();
        if (!serverState.isEnabledOnServer() || !serverState.isActive() || !serverState.getConfig().isAllowWireframeRendering()) {
            return;
        }

        BlockPos origin = client.getBlockLookUpdateHandler().getLastLookedAtBlockPos();
        VoxelShape shape = serverState.getVeinMineResultShape();
        if (origin == null || shape == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Profiler.get().push("veinMinerWireframe");

        /*
         * Massive credit to FTB-Ultimine for help with this rendering code. I don't think I
         * would have been able to figure this out myself... I'm not familiar with navigating the
         * Minecraft codebase. But this does make a lot of sense and it helped a lot. I'm very
         * appreciative for open source code :)
         *
         * https://github.com/FTBTeam/FTB-Ultimine
         */

        // Calculate the stack
        PoseStack stack = context.matrixStack();
        if (stack == null) { // Should never be null, but maybe in the future it will.
            Profiler.get().pop();
            return;
        }

        Vec3 position = client.getEntityRenderDispatcher().camera.getPosition();

        stack.pushPose();
        stack.translate(origin.getX() - position.x, origin.getY() - position.y, origin.getZ() - position.z);

        Matrix4f matrix = stack.last().pose();

        // Buffer vertices
        BufferSource source = client.renderBuffers().bufferSource();

        // Full wireframe drawing
        VertexConsumer consumer = source.getBuffer(VeinMinerRenderType.getWireframe());

        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            consumer.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(255, 255, 255, 255);
            consumer.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(255, 255, 255, 255);
        });

        source.endBatch(VeinMinerRenderType.getWireframe());

        // Transparent drawing
        VertexConsumer bufferTransparent = source.getBuffer(VeinMinerRenderType.getWireframeTransparent());

        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            bufferTransparent.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(255, 255, 255, 20);
            bufferTransparent.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(255, 255, 255, 20);
        });

        source.endBatch(VeinMinerRenderType.getWireframeTransparent());

        stack.popPose();
        Profiler.get().pop();
    }

}
