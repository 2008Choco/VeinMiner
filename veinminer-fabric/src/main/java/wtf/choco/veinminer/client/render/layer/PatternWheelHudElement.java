package wtf.choco.veinminer.client.render.layer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

/**
 * A {@link VeinMinerHudElement} for the pattern selection wheel in the top left.
 */
public final class PatternWheelHudElement extends VeinMinerHudElement {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("veinminer_companion", "pattern_wheel");

    private static final int STAY_TICKS = 60; // 3 seconds
    private static final int FADE_TICKS = 4; // 0.2 seconds
    private static final int TOTAL_TICKS = STAY_TICKS + (FADE_TICKS * 2);

    private int remainingTicks = 0;

    public PatternWheelHudElement(VeinMinerClient client) {
        super(client);
    }

    @Override
    public void render(@NotNull FabricServerState serverState, @NotNull GuiGraphicsExtractor graphics, @NotNull DeltaTracker delta) {
        Profiler.get().push("veinminerPatternWheel");

        int colour = ARGB.color(calculateAlpha(delta), 0xFFFFFF);
        String before = serverState.getPreviousPattern().toString();
        String selected = serverState.getSelectedPattern().toString();
        String after = serverState.getNextPattern().toString();

        Minecraft minecraft = Minecraft.getInstance();
        Matrix3x2fStack stack = graphics.pose();
        stack.pushMatrix();
        stack.scale(1.1F);
        graphics.text(minecraft.font, Component.literal(selected), 4, minecraft.font.lineHeight, colour);

        stack.scale(0.6F);
        graphics.text(minecraft.font, Component.literal(before), 7, 4, colour);
        graphics.text(minecraft.font, Component.literal(after), 7, 5 + (minecraft.font.lineHeight * 3), colour);
        stack.popMatrix();

        Profiler.get().pop();
    }

    private int calculateAlpha(@NotNull DeltaTracker delta) {
        float actualRemainingMs = (remainingTicks - delta.getGameTimeDeltaPartialTick(false));

        // Calculate alpha based on progress of fade in/out times
        int alpha = 255;
        if (remainingTicks >= (STAY_TICKS + FADE_TICKS)) { // Fade in
            float elapsed = TOTAL_TICKS - actualRemainingMs;
            alpha = (int) (elapsed * 255.0F / (float) FADE_TICKS);
        } else if (remainingTicks <= FADE_TICKS) { // Fade out
            alpha = (int) (actualRemainingMs * 255.0F / (float) FADE_TICKS);
        }

        return Math.clamp(alpha, 0, 255);
    }

    @Override
    public boolean shouldRender(@NotNull FabricServerState serverState) {
        return serverState.getConfig().isAllowPatternSwitchingKeybind() && serverState.hasPatternKeys() && remainingTicks > 0;
    }

    public void tick() {
        if (remainingTicks > 0) {
            this.remainingTicks--;
        }
    }

    /**
     * Push a render cycle to the component.
     * <p>
     * If this wheel is not being rendered, it will start rendering. If it is fading in, it will
     * continue to fade in. If it is during its stay period, it will reset to the start of the stay
     * period. If it is fading out, it will start fading in starting from the current fade out time.
     */
    public void pushRender() {
        if (remainingTicks <= 0) { // If not rendered, set to max time
            this.remainingTicks = TOTAL_TICKS;
        }
        else if (remainingTicks < FADE_TICKS) { // If fading out, fade back in starting at current fade out point
            this.remainingTicks = TOTAL_TICKS - remainingTicks;
        }
        else if (remainingTicks < (TOTAL_TICKS - FADE_TICKS)) { // If already faded in (during stay time), skip fade in time
            this.remainingTicks = TOTAL_TICKS - FADE_TICKS;
        }
    }

}
