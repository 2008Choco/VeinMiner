package wtf.choco.veinminer.client.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

/**
 * A renderer for wireframed voxel shapes.
 */
public final class WireframeShapeRenderer {

    private static final int WIREFRAME_COLOR = 0xFFFFFF;
    private static final int WIREFRAME_COLOR_SOLID = ARGB.color(255, WIREFRAME_COLOR);
    private static final int WIREFRAME_COLOR_TRANSLUCENT = ARGB.color(20, WIREFRAME_COLOR);

    private static final Supplier<VoxelShape> DEBUG_SHAPE = Suppliers.memoize(() -> Shapes.or(
            Shapes.block(),
            Shapes.block().move(0, 1, 0),
            Shapes.block().move(0, -1, 0),
            Shapes.block().move(1, 0, 0),
            Shapes.block().move(-1, 0, 0),
            Shapes.block().move(0, 0, 1),
            Shapes.block().move(0, 0, -1)
    ));

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
        Minecraft client = Minecraft.getInstance();
        if (!this.client.hasServerState()) {
            return;
        }

        FabricServerState serverState = this.client.getServerState();
        if (!serverState.isEnabledOnServer() || !serverState.isActive() || !serverState.getConfig().isAllowWireframeRendering()) {
            return;
        }

        BlockPos blockPos = this.client.getBlockLookUpdateHandler().getLastLookedAtBlockPos();
        if (blockPos == null) {
            return;
        }

        VoxelShape shape = serverState.getVeinMineResultShape();
        if (shape == null) {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                return;
            }

            shape = DEBUG_SHAPE.get();
        }

        Profiler.get().push("veinMinerWireframe");

        Vec3 camera = client.getEntityRenderDispatcher().camera.getPosition();
        double relX = blockPos.getX() - camera.x;
        double relY = blockPos.getY() - camera.y;
        double relZ = blockPos.getZ() - camera.z;

        PoseStack stack = context.matrices();
        BufferSource source = client.renderBuffers().bufferSource();
        this.renderShape(shape, source, VeinMinerRenderType.wireframe(), stack, relX, relY, relZ, WIREFRAME_COLOR_SOLID);
        this.renderShape(shape, source, VeinMinerRenderType.wireframeTransparent(), stack, relX, relY, relZ, WIREFRAME_COLOR_TRANSLUCENT);

        Profiler.get().pop();
    }

    private void renderShape(VoxelShape shape, BufferSource source, RenderType renderType, PoseStack stack, double relX, double relY, double relZ, int color) {
        Pose pose = stack.last();
        VertexConsumer consumer = source.getBuffer(renderType);
        shape.forAllEdges((x, y, z, dx, dy, dz) -> renderEdge(
                consumer,
                pose,
                (float) (x + relX),
                (float) (y + relY),
                (float) (z + relZ),
                (float) (dx + relX),
                (float) (dy + relY),
                (float) (dz + relZ),
                color
        ));
        source.endLastBatch();
    }

    private void renderEdge(VertexConsumer consumer, Pose pose, float x, float y, float z, float dx, float dy, float dz, int color) {
        Vector3f normal = new Vector3f(dx - x, dy - y, dz - z).normalize();
        consumer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, normal);
        consumer.addVertex(pose, dx, dy, dz).setColor(color).setNormal(pose, normal);
    }

}
