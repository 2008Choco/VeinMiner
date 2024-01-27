package wtf.choco.veinminer.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;

import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.renderer.RenderStateShard.*;

/**
 * A container class for {@link RenderType RenderTypes} used by VeinMiner.
 */
public final class VeinMinerRenderType {

    private static final RenderType WIREFRAME = RenderType.create("veinminer_wireframe", DefaultVertexFormat.POSITION_COLOR, Mode.DEBUG_LINES, 256, CompositeState.builder()
            .setLineState(new LineStateShard(OptionalDouble.empty()))
            .setLayeringState(NO_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(CULL)
            .setShaderState(POSITION_COLOR_SHADER)
            .createCompositeState(false));

    private static final RenderType WIREFRAME_TRANSPARENT = RenderType.create("veinminer_wireframe_transparent", DefaultVertexFormat.POSITION_COLOR, Mode.DEBUG_LINES, 256, CompositeState.builder()
            .setLineState(new LineStateShard(OptionalDouble.empty()))
            .setLayeringState(NO_LAYERING)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setCullState(CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .setShaderState(POSITION_COLOR_SHADER)
            .createCompositeState(false));

    /**
     * Get the wireframe {@link RenderType}.
     *
     * @return the wireframe render type
     */
    @NotNull
    public static RenderType getWireframe() {
        return WIREFRAME;
    }

    /**
     * Get the transparent wireframe {@link RenderType}.
     *
     * @return the transparent wireframe render type
     */
    @NotNull
    public static RenderType getWireframeTransparent() {
        return WIREFRAME_TRANSPARENT;
    }

}
