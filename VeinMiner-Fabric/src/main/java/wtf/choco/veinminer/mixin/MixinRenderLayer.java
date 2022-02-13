package wtf.choco.veinminer.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhase;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("unused") // Parameter names
@Mixin(RenderLayer.class)
public interface MixinRenderLayer {

    @Invoker("of")
    public static MultiPhase of(String name, VertexFormat vertexFormat, DrawMode drawMode, int expectedBufferSize, MultiPhaseParameters phaseData) {
        throw new AssertionError();
    }

}
