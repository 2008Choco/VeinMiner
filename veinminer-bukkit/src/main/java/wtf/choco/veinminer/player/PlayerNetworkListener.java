package wtf.choco.veinminer.player;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.network.NetworkUtil;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundToggleVeinMiner;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.AttributeUtil;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.VMEventFactory;

/**
 * A {@link VeinMinerServerboundMessageListener} for a {@link VeinMinerPlayer}.
 * <p>
 * <strong>NOTE:</strong> Not part of the public API. This class is intended for internal use only.
 * Most methods in this interface can be found mirrored in {@link VeinMinerPlayer} instead. Anything
 * not in VeinMinerPlayer is not meant to be called.
 */
@Internal
public final class PlayerNetworkListener implements VeinMinerServerboundMessageListener {

    private boolean clientReady = false;
    private boolean usingClientMod = false;
    private boolean clientKeyPressed = false;
    private Queue<Runnable> onClientReadyTasks = new ConcurrentLinkedQueue<>();

    private final VeinMinerPlayer player;

    PlayerNetworkListener(@NotNull VeinMinerPlayer player) {
        Preconditions.checkArgument(player != null, "player must not be null");
        this.player = player;
    }

    /**
     * Check whether or not the client is ready to receive messages.
     * <p>
     * This method will only be true if {@link #isUsingClientMod()} is {@code true}, the client has
     * successfully shaken hands with the server, is capable of being sent a client message, and
     * has been synchronized with the server as per the protocol specification.
     *
     * @return true if the client is ready, false otherwise
     */
    public boolean isClientReady() {
        return clientReady;
    }

    /**
     * Check whether or not this player is using the client mod.
     *
     * @return true if using client mod, false otherwise
     */
    public boolean isUsingClientMod() {
        return usingClientMod;
    }

    /**
     * Check whether or not vein miner is active as a result of this user's client mod. This method will
     * always return {@code false} if the player {@link #isUsingClientMod() is not using the client mod}.
     *
     * @return true if active, false otherwise
     */
    public boolean isClientKeyPressed() {
        return clientKeyPressed;
    }

    /**
     * Add a {@link Runnable} task to execute when this client is ready.
     *
     * @param task the task
     */
    public void addOnClientReadyTask(@NotNull Runnable task) {
        Preconditions.checkArgument(task != null, "task must not be null");
        this.onClientReadyTasks.add(task);
    }

    @Override
    public void handleHandshake(@NotNull ServerboundHandshake message) {
        int serverProtocolVersion = VeinMiner.PROTOCOL.getVersion();
        if (serverProtocolVersion != message.getProtocolVersion()) {
            this.player.getPlayer().kickPlayer("Your client-side version of VeinMiner (for Bukkit) is " + (serverProtocolVersion > message.getProtocolVersion() ? "out of date. Please update." : "too new. Please downgrade."));
            return;
        }

        this.usingClientMod = true;
        this.player.setActivationStrategy(ActivationStrategy.CLIENT);
        this.player.setDirty(false); // We can force dirty = false. Data hasn't loaded yet, but we still want to set the strategy to client automatically

        /*
         * Let the client know whether or not the client is even allowed.
         * We send this one tick later so we know that the player's connection has been initialized
         */
        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.player.sendMessage(new ClientboundHandshakeResponse());

            // Synchronize all registered patterns to the client
            PatternRegistry patternRegistry = plugin.getPatternRegistry();

            List<NamespacedKey> patternKeys = new ArrayList<>();
            patternRegistry.getPatterns().forEach(pattern -> patternKeys.add(pattern.getKey()));
            VeinMiningPattern defaultPattern = plugin.getConfiguration().getDefaultVeinMiningPattern();

            // Move the default pattern to the start if it wasn't already there
            NamespacedKey defaultPatternKey = defaultPattern.getKey();
            if (patternKeys.size() > 1 && patternKeys.remove(defaultPatternKey)) {
                patternKeys.add(0, defaultPatternKey);
            }

            // Don't send any patterns to which the player does not have access
            patternKeys.removeIf(patternKey -> {
                VeinMiningPattern pattern = patternRegistry.get(patternKey);
                if (pattern == null) {
                    return true;
                }

                String permission = pattern.getPermission();
                return permission != null && !player.getPlayer().hasPermission(permission);
            });

            this.player.sendMessage(new ClientboundSyncRegisteredPatterns(NetworkUtil.toNetwork(patternKeys)));
            this.player.sendMessage(new ClientboundSetConfig(player.getClientConfig()));

            // The client is ready, accept post-client init tasks now
            this.clientReady = true;

            Runnable runnable;
            while ((runnable = onClientReadyTasks.poll()) != null) {
                runnable.run();
            }
        }, 1);
    }

    @Override
    public void handleToggleVeinMiner(@NotNull ServerboundToggleVeinMiner message) {
        if (!player.getClientConfig().isAllowActivationKeybind()) {
            return;
        }

        if (!VMEventFactory.callPlayerClientActivateVeinMinerEvent(player.getPlayer(), message.isActivated())) {
            return;
        }

        this.clientKeyPressed = message.isActivated();
    }

    @Override
    public void handleRequestVeinMine(@NotNull ServerboundRequestVeinMine message) {
        Player bukkitPlayer = player.getPlayer();
        ItemStack itemStack = bukkitPlayer.getInventory().getItemInMainHand();
        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(itemStack);

        if (category == null) {
            this.player.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        double reachDistance = AttributeUtil.getReachDistance(bukkitPlayer);
        RayTraceResult rayTraceResult = bukkitPlayer.rayTraceBlocks(reachDistance);
        if (rayTraceResult == null) {
            this.player.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        Block targetBlock = rayTraceResult.getHitBlock();
        BlockFace targetBlockFace = rayTraceResult.getHitBlockFace();

        if (targetBlock == null || targetBlockFace == null) {
            this.player.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        // Validate the client's target block against the server's client block. It should be within 2 blocks of the client's target
        BlockPosition clientTargetBlock = message.getPosition();
        if (clientTargetBlock.distanceSquared(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()) >= 4) {
            this.player.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        targetBlock = targetBlock.getWorld().getBlockAt(clientTargetBlock.x(), clientTargetBlock.y(), clientTargetBlock.z());
        BlockData targetBlockData = targetBlock.getBlockData();

        VeinMinerManager veinMinerManager = plugin.getVeinMinerManager();
        VeinMinerBlock vmBlock = veinMinerManager.getVeinMinerBlock(targetBlockData, category);

        if (vmBlock == null) {
            this.player.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        BlockList aliasBlockList = veinMinerManager.getAliases(vmBlock);
        List<Block> blocks = player.getVeinMiningPattern().allocateBlocks(targetBlock, targetBlockFace, vmBlock, category.getConfiguration(), aliasBlockList);

        this.player.sendMessage(new ClientboundVeinMineResults(blocks.parallelStream().map(block -> new BlockPosition(block.getX(), block.getY(), block.getZ())).toList()));
    }

    @Override
    public void handleSelectPattern(@NotNull ServerboundSelectPattern message) {
        if (!player.getClientConfig().isAllowPatternSwitchingKeybind()) {
            return;
        }

        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        VeinMiningPattern pattern = plugin.getPatternRegistry().getOrDefault(NetworkUtil.toBukkit(message.getPatternKey()), plugin.getConfiguration().getDefaultVeinMiningPattern());
        String patternPermission = pattern.getPermission();

        if (patternPermission != null && !player.getPlayer().hasPermission(patternPermission)) {
            return;
        }

        PlayerVeinMiningPatternChangeEvent event = VMEventFactory.callPlayerVeinMiningPatternChangeEvent(player.getPlayer(), player.getVeinMiningPattern(), pattern, PlayerVeinMiningPatternChangeEvent.Cause.CLIENT);
        if (event.isCancelled()) {
            return;
        }

        this.player.setVeinMiningPattern(event.getNewPattern());
    }

}
