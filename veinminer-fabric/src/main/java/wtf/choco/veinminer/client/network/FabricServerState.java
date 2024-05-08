package wtf.choco.veinminer.client.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.network.Message;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.network.fabric.FabricMessageReceiver;
import wtf.choco.network.fabric.RawDataPayload;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundSelectPattern;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * The client's state on a connected server.
 */
public final class FabricServerState implements FabricMessageReceiver, VeinMinerClientboundMessageListener {

    private static final VoxelShape WIDE_CUBE = Shapes.box(-0.005, -0.005, -0.005, 1.005, 1.005, 1.005);

    private boolean enabledOnServer, active;
    private ClientConfig config = ClientConfig.builder()
            .allowActivationKeybind(false)
            .allowPatternSwitchingKeybind(false)
            .allowWireframeRendering(false)
            .build();

    private int selectedPatternIndex = 0;
    private List<NamespacedKey> patternKeys = null;

    private VoxelShape veinMineResultShape;

    private final VeinMinerClient client;

    /**
     * Construct a new {@link FabricServerState}.
     *
     * @param client the client instance
     * @param minecraft the {@link Minecraft} instance
     */
    public FabricServerState(@NotNull VeinMinerClient client, @NotNull Minecraft minecraft) {
        this.client = client;

        // We'll enable VeinMiner if we're in single player development mode, just for testing
        if (minecraft.hasSingleplayerServer() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
            this.config = new ClientConfig();
            this.enabledOnServer = true;
            this.patternKeys = List.of(
                NamespacedKey.of("veinminer", "default"),
                NamespacedKey.of("veinminer", "tunnel"),
                NamespacedKey.of("veinminer", "staircase_up"),
                NamespacedKey.of("veinminer", "staircase_down")
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

        this.sendMessage(new ServerboundSelectPattern(patternKeys.get(selectedPatternIndex)));
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
    public void sendMessage(@NotNull RawDataPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    /**
     * Send a {@link Message} to this {@link FabricServerState} across the
     * {@link VeinMiner#PROTOCOL VeinMiner protocol}.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull Message<VeinMinerServerboundMessageListener> message) {
        VeinMiner.PROTOCOL.sendMessageToServer(this, message);
    }

    @Override
    public void handleHandshakeResponse(@NotNull ClientboundHandshakeResponse message) {
        this.enabledOnServer = true;
    }

    @Override
    public void handleSyncRegisteredPatterns(@NotNull ClientboundSyncRegisteredPatterns message) {
        boolean firstSync = (patternKeys == null);
        NamespacedKey previouslySelectedPatternKey = (!firstSync && selectedPatternIndex < patternKeys.size()) ? patternKeys.get(selectedPatternIndex) : null;

        this.patternKeys = new ArrayList<>(message.getKeys());

        // Reselect the index that was previously selected (if it changed), or default to the first if it does not exist anymore
        if (!firstSync && previouslySelectedPatternKey != null) {
            this.selectedPatternIndex = Math.max(patternKeys.indexOf(previouslySelectedPatternKey), 0);
        }
    }

    @Override
    public void handleSetConfig(@NotNull ClientboundSetConfig message) {
        this.config = message.getConfig();
    }

    @Override
    public void handleVeinMineResults(@NotNull ClientboundVeinMineResults message) {
        this.resetShape();

        BlockPos origin = client.getBlockLookUpdateHandler().getLastLookedAtBlockPos();
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
    public void handleSetPattern(@NotNull ClientboundSetPattern message) {
        if (patternKeys == null) {
            return;
        }

        this.selectedPatternIndex = Math.max(patternKeys.indexOf(message.getPatternKey()), 0);
    }

}
