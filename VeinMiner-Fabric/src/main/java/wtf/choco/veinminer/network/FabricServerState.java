package wtf.choco.veinminer.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundSelectPattern;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * The client's state on a connected server.
 */
public final class FabricServerState implements ClientboundPluginMessageListener, MessageReceiver {

    private static final VoxelShape WIDE_CUBE = Shapes.box(-0.005, -0.005, -0.005, 1.005, 1.005, 1.005);

    private boolean enabledOnServer, active;
    private ClientConfig config = ClientConfig.builder()
            .allowActivationKeybind(false)
            .allowPatternSwitchingKeybind(false)
            .allowWireframeRendering(false)
            .build();

    private int selectedPatternIndex = 0;
    private List<NamespacedKey> patternKeys = null;

    private BlockPos lastLookedAtBlockPos;
    private Direction lastLookedAtBlockFace;
    private VoxelShape veinMineResultShape;

    /**
     * Construct a new {@link FabricServerState}.
     *
     * @param client the {@link Minecraft} instance
     */
    public FabricServerState(@NotNull Minecraft client) {
        // We'll enable VeinMiner if we're in single player development mode, just for testing
        if (client.hasSingleplayerServer() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
            this.config = new ClientConfig();
            this.patternKeys = List.of(
                NamespacedKey.veinminer("default"),
                NamespacedKey.veinminer("tunnel"),
                NamespacedKey.veinminer("staircase_up"),
                NamespacedKey.veinminer("staircase_down")
            );
        }
    }

    /**
     * Get the {@link ClientConfig} for the mod.
     *
     * @return the config
     */
    @NotNull
    public ClientConfig getConfig() {
        return config;
    }

    /**
     * Check whether or not VeinMiner is enabled on the server.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabledOnServer() {
        return enabledOnServer;
    }

    /**
     * Set whether or not vein miner is active.
     *
     * @param active the new active state
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check whether or not vein mine is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get the index of the currently selected pattern.
     *
     * @return the selected pattern index
     */
    public int getSelectedPatternIndex() {
        return selectedPatternIndex;
    }

    /**
     * Get the {@link NamespacedKey} of the currently selected pattern.
     *
     * @return the selected pattern key
     */
    @NotNull
    public NamespacedKey getSelectedPattern() {
        return patternKeys.get(selectedPatternIndex);
    }

    /**
     * Get the {@link NamespacedKey} of the next pattern to be selected.
     *
     * @return the next pattern key
     */
    @NotNull
    public NamespacedKey getNextPattern() {
        return patternKeys.get((selectedPatternIndex + 1) % patternKeys.size());
    }

    /**
     * Get the {@link NamespacedKey} of the previous pattern to be selected.
     *
     * @return the previous pattern key
     */
    @NotNull
    public NamespacedKey getPreviousPattern() {
        int index = (selectedPatternIndex - 1) % patternKeys.size();

        if (index < 0) {
            index = patternKeys.size() + index;
        }

        return patternKeys.get(index);
    }

    /**
     * Change to an adjacent pattern.
     *
     * @param next whether or not to move to the next or previous index
     *
     * @return true if the pattern was changed, false if the client does not recognize any
     * pattern yet and could not be changed
     */
    public boolean changePattern(boolean next) {
        if (patternKeys == null) {
            return false;
        }

        this.selectedPatternIndex += (next ? 1 : -1);
        this.selectedPatternIndex %= patternKeys.size();

        if (selectedPatternIndex < 0) {
            this.selectedPatternIndex = patternKeys.size() + selectedPatternIndex;
        }

        VeinMiner.PROTOCOL.sendMessageToServer(this, new PluginMessageServerboundSelectPattern(patternKeys.get(selectedPatternIndex)));

        return true;
    }

    /**
     * Get a {@link List} of all pattern keys known to the client.
     *
     * @return all known pattern keys
     */
    @NotNull
    public List<NamespacedKey> getPatternKeys() {
        return (patternKeys != null) ? patternKeys : Collections.emptyList();
    }

    /**
     * Check whether or not any pattern keys are currently available.
     *
     * @return true if there are pattern keys, false otherwise
     */
    public boolean hasPatternKeys() {
        return patternKeys != null && !patternKeys.isEmpty();
    }

    /**
     * Set the last {@link BlockPos} and {@link Direction} the client has looked at.
     *
     * @param position the last looked at position
     * @param blockFace the last looked at block face
     */
    public void setLastLookedAt(@Nullable BlockPos position, @Nullable Direction blockFace) {
        this.lastLookedAtBlockPos = position;
        this.lastLookedAtBlockFace = blockFace;

        if (position == null || blockFace == null) {
            this.resetShape();
        }
    }

    /**
     * Get the {@link BlockPos} last looked at by the client, or null if the client has not
     * looked at a block.
     *
     * @return the last looked at block position, or null if none
     */
    @Nullable
    public BlockPos getLastLookedAtBlockPos() {
        return lastLookedAtBlockPos;
    }

    /**
     * Get the block face {@link Direction} last looked at by the client, or null if the client
     * has not looked at a block.
     *
     * @return the last looked at block face, or null if none
     */
    @Nullable
    public Direction getLastLookedAtBlockFace() {
        return lastLookedAtBlockFace;
    }

    /**
     * Get the {@link VoxelShape} outlining the last vein mine result received from the server.
     *
     * @return the last vein mine result shape, or null if none
     */
    @Nullable
    public VoxelShape getVeinMineResultShape() {
        return veinMineResultShape;
    }

    /**
     * Reset the last vein mine result shape.
     */
    public void resetShape() {
        this.veinMineResultShape = null;
    }

    @Override
    public void sendMessage(@NotNull NamespacedKey channel, byte[] message) {
        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeBytes(message);

        ClientPlayNetworking.send(new ResourceLocation(channel.namespace(), channel.key()), byteBuf);
    }

    @Override
    public void handleHandshakeResponse(@NotNull PluginMessageClientboundHandshakeResponse message) {
        this.enabledOnServer = true;
    }

    @Override
    public void handleSyncRegisteredPatterns(@NotNull PluginMessageClientboundSyncRegisteredPatterns message) {
        boolean firstSync = (patternKeys == null);
        NamespacedKey previouslySelectedPatternKey = (!firstSync && selectedPatternIndex < patternKeys.size()) ? patternKeys.get(selectedPatternIndex) : null;

        this.patternKeys = new ArrayList<>(message.getKeys());

        // Reselect the index that was previously selected (if it changed), or default to the first if it does not exist anymore
        if (!firstSync && previouslySelectedPatternKey != null) {
            this.selectedPatternIndex = Math.max(patternKeys.indexOf(previouslySelectedPatternKey), 0);
        }
    }

    @Override
    public void handleSetConfig(@NotNull PluginMessageClientboundSetConfig message) {
        this.config = message.getConfig();
    }

    @Override
    public void handleVeinMineResults(@NotNull PluginMessageClientboundVeinMineResults message) {
        this.resetShape();

        BlockPos origin = lastLookedAtBlockPos;
        if (origin == null) {
            return;
        }

        List<BlockPosition> positions = message.getBlockPositions();
        if (positions.isEmpty()) {
            return;
        }

        int i = 0;
        VoxelShape[] shapes = new VoxelShape[positions.size()];

        for (BlockPosition position : positions) {
            int offsetX = position.x() - origin.getX(), offsetY = position.y() - origin.getY(), offsetZ = position.z() - origin.getZ();

            VoxelShape shape = WIDE_CUBE;
            if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
                shape = shape.move(offsetX, offsetY, offsetZ);
            }

            shapes[i++] = shape;
        }

        this.veinMineResultShape = Shapes.or(shapes[0], shapes);
    }

    @Override
    public void handleSetPattern(@NotNull PluginMessageClientboundSetPattern message) {
        this.selectedPatternIndex = Math.max(patternKeys.indexOf(message.getPatternKey()), 0);
    }

}
