package wtf.choco.veinminer.client.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

/**
 * A renderer for wireframed voxel shapes.
 */
public final class WireframeShapeRenderer {

    private static final int WIREFRAME_COLOR = 0xFFFFFF;
    private static final int WIREFRAME_COLOR_SOLID = ARGB.color(102, WIREFRAME_COLOR);
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
     *
     * @return true if the block outline should be rendered, or false to hide it
     */
    public boolean render(@NotNull LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (!this.client.hasServerState()) {
            return true;
        }

        FabricServerState serverState = this.client.getServerState();
        if (!serverState.isEnabledOnServer() || !serverState.isActive() || !serverState.getConfig().isAllowWireframeRendering()) {
            return true;
        }

        BlockPos blockPos = this.client.getBlockLookUpdateHandler().getLastLookedAtBlockPos();
        if (blockPos == null) {
            return true;
        }

        VoxelShape shape = serverState.getVeinMineResultShape();
        if (shape == null) {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                return true;
            }

            shape = DEBUG_SHAPE.get();
        }

        Profiler.get().push("veinMinerWireframe");

        Vec3 camera = client.getEntityRenderDispatcher().camera.position();
        double relX = blockPos.getX() - camera.x;
        double relY = blockPos.getY() - camera.y;
        double relZ = blockPos.getZ() - camera.z;

        SubmitNodeCollector collector = context.submitNodeCollector();
        float lineWidth = context.gameRenderer().gameRenderState().windowRenderState.appropriateLineWidth;

        PoseStack stack = context.poseStack();
        stack.pushPose();
        stack.translate(relX, relY, relZ);
        this.renderShape(shape, collector, VeinMinerRenderType.wireframe(), stack, WIREFRAME_COLOR_SOLID, lineWidth, false);
        this.renderShape(shape, collector, VeinMinerRenderType.wireframeTransparent(), stack, WIREFRAME_COLOR_TRANSLUCENT, lineWidth, true);
        stack.popPose();

        Profiler.get().pop();
        return true;
    }

    private void renderShape(VoxelShape shape, SubmitNodeCollector collector, RenderType renderType, PoseStack stack, int color, float lineWidth, boolean afterTerrain) {
        collector.submitShapeOutline(stack, shape, renderType, color, lineWidth, afterTerrain);
    }

}
