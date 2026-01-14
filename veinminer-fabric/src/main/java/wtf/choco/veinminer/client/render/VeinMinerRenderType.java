package wtf.choco.veinminer.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class VeinMinerRenderType {

    private static final RenderPipeline PIPELINE_WIREFRAME = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("veinminer_companion", "pipeline/wireframe"))
            .withColorWrite(true)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    private static final RenderPipeline PIPELINE_WIREFRAME_TRANSPARENT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("veinminer_companion", "pipeline/wireframe_transparent"))
            .withColorWrite(true)
            .withDepthWrite(true)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build());

    private static final RenderType WIREFRAME = RenderType.create("veinminer_companion:wireframe", RenderSetup.builder(PIPELINE_WIREFRAME)
            .setLayeringTransform(LayeringTransform.NO_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    );

    private static final RenderType WIREFRAME_TRANSPARENT = RenderType.create("veinminer_companion:wireframe_transparent", RenderSetup.builder(PIPELINE_WIREFRAME_TRANSPARENT)
            .setLayeringTransform(LayeringTransform.NO_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    );

    private VeinMinerRenderType() { }

    public static RenderType wireframe() {
        return WIREFRAME;
    }

    public static RenderType wireframeTransparent() {
        return WIREFRAME_TRANSPARENT;
    }

}
