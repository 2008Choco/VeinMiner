package wtf.choco.veinminer.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class VeinMinerRenderType {

    private static final RenderPipeline PIPELINE_WIREFRAME = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath("veinminer_companion", "pipeline/wireframe"))
            .withColorWrite(true)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    private static final RenderPipeline PIPELINE_WIREFRAME_TRANSPARENT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath("veinminer_companion", "pipeline/wireframe_transparent"))
            .withColorWrite(true)
            .withDepthWrite(true)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build());

    private static final RenderType WIREFRAME = RenderType.create("veinminer_companion:wireframe", 1536,
            PIPELINE_WIREFRAME,
            RenderType.CompositeState.builder()
                .setLineState(new LineStateShard(OptionalDouble.of(1.0D))) // Line thickness
                .setLayeringState(RenderStateShard.NO_LAYERING)
                .createCompositeState(false)
    );

    private static final RenderType WIREFRAME_TRANSPARENT = RenderType.create("veinminer_companion:wireframe_transparent", 1536,
            PIPELINE_WIREFRAME_TRANSPARENT,
            RenderType.CompositeState.builder()
                .setLineState(new LineStateShard(OptionalDouble.of(2.0D))) // Line thickness (thicker in the back *wink wink*)
                .setLayeringState(RenderStateShard.NO_LAYERING)
                .createCompositeState(false)
    );

    private VeinMinerRenderType() { }

    public static RenderType wireframe() {
        return WIREFRAME;
    }

    public static RenderType wireframeTransparent() {
        return WIREFRAME_TRANSPARENT;
    }

}
