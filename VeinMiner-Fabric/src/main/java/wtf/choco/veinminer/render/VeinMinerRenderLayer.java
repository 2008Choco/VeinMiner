package wtf.choco.veinminer.render;

import java.util.OptionalDouble;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.mixin.MixinRenderLayer;

public final class VeinMinerRenderLayer extends RenderLayer { // extending RenderLayer grants us access to its protected constants

    private static final RenderLayer WIREFRAME = MixinRenderLayer.of("veinminer_wireframe", VertexFormats.POSITION_COLOR, DrawMode.DEBUG_LINES, 256, MultiPhaseParameters.builder()
            .lineWidth(new LineWidth(OptionalDouble.empty()))
            .layering(NO_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .writeMaskState(COLOR_MASK)
            .cull(ENABLE_CULLING)
            .shader(COLOR_SHADER)
            .build(false));

    private static final RenderLayer WIREFRAME_TRANSPARENT = MixinRenderLayer.of("veinminer_wireframe_transparent", VertexFormats.POSITION_COLOR, DrawMode.DEBUG_LINES, 256, MultiPhaseParameters.builder()
            .lineWidth(new LineWidth(OptionalDouble.empty()))
            .layering(NO_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .writeMaskState(ALL_MASK)
            .cull(ENABLE_CULLING)
            .depthTest(ALWAYS_DEPTH_TEST)
            .shader(COLOR_SHADER)
            .build(false));

    // Exists just to satisfy the compiler
    private VeinMinerRenderLayer(String name, VertexFormat vertexFormat, DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    @NotNull
    public static RenderLayer getWireframe() {
        return WIREFRAME;
    }

    @NotNull
    public static RenderLayer getWireframeTransparent() {
        return WIREFRAME_TRANSPARENT;
    }

}
