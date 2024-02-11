package wtf.choco.veinminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.network.Message;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.network.receiver.MessageReceiver;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.manager.VeinMinerPlayerManager;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundToggleVeinMiner;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.VMEventFactory;

/**
 * A player wrapper containing player-related data for VeinMiner, as well as a network
 * handler for vein miner protocol messages.
 */
public final class VeinMinerPlayer implements MessageReceiver, VeinMinerServerboundMessageListener {

    private ActivationStrategy activationStrategy;
    private final Set<VeinMinerToolCategory> disabledCategories = new HashSet<>();
    private VeinMiningPattern veinMiningPattern;

    private boolean dirty = false;

    private boolean clientReady = false;
    private Queue<Runnable> onClientReady = new ConcurrentLinkedQueue<>();

    private boolean usingClientMod = false;
    private boolean clientKeyPressed = false;

    private boolean veinMining = false;

    private ClientConfig clientConfig;

    private final Player player;

    /**
     * Construct a new {@link VeinMinerPlayer}.
     * <p>
     * This is an internal method. To get an instance of VeinMinerPlayer, the {@link VeinMinerPlayerManager}
     * should be used instead. Constructing a new instance of this class may have unintended side-effects
     * and will not have accurate information tracked by VeinMiner.
     *
     * @param player the player
     * @param clientConfig the client configuration
     *
     * @see VeinMinerPlayerManager
     */
    @Internal
    public VeinMinerPlayer(@NotNull Player player, @NotNull ClientConfig clientConfig) {
        this.player = player;
        this.clientConfig = clientConfig;
    }

    /**
     * Get the {@link Player} wrapped by this vein miner player.
     *
     * @return the platform player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link UUID} of this player.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    /**
     * Set whether or not the given {@link VeinMinerToolCategory} is enabled.
     *
     * @param category the category to change
     * @param enabled whether or not the category is enabled
     *
     * @return true if the category state was changed, false if the category remains unchanged
     */
    public boolean setVeinMinerEnabled(@NotNull VeinMinerToolCategory category, boolean enabled) {
        boolean changed = (enabled) ? disabledCategories.remove(category) : disabledCategories.add(category);
        this.dirty |= changed;
        return changed;
    }

    /**
     * Set whether or not vein miner is enabled entirely.
     * <p>
     * If {@code true} and one or more categories are disabled, it will enable all disabled
     * categories. If {code false} not all categories have been disabled, it will disable all
     * remaining categories.
     *
     * @param enabled whether or not to enable vein miner
     *
     * @return true if at least one category was changed as a result of the enable toggle
     */
    public boolean setVeinMinerEnabled(boolean enabled) {
        boolean changed;

        if (enabled) {
            changed = !disabledCategories.isEmpty();
            this.disabledCategories.clear();
        } else {
            changed = disabledCategories.addAll(VeinMinerPlugin.getInstance().getToolCategoryRegistry().getAll());
        }

        this.dirty |= changed;
        return changed;
    }

    /**
     * Check whether or not the given {@link VeinMinerToolCategory} is enabled.
     *
     * @param category the category to check
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVeinMinerEnabled(@NotNull VeinMinerToolCategory category) {
        return !disabledCategories.contains(category);
    }

    /**
     * Check whether or not vein miner is completely enabled.
     * <p>
     * If at least one category is disabled (according to {@link #isVeinMinerEnabled(VeinMinerToolCategory)}),
     * this method will return {@code false}.
     *
     * @return true if fully enabled, false if at least one category is disabled
     */
    public boolean isVeinMinerEnabled() {
        return disabledCategories.isEmpty();
    }

    /**
     * Check whether or not vein miner is completely disabled.
     * <p>
     * If at least one category is enabled (according to {@link #isVeinMinerEnabled(VeinMinerToolCategory)}),
     * this method will return {@code false}.
     *
     * @return true if fully disabled, false if at least one category is enabled
     */
    public boolean isVeinMinerDisabled() {
        return disabledCategories.size() >= VeinMinerPlugin.getInstance().getToolCategoryRegistry().size();
    }

    /**
     * Check whether or not vein miner has been partially disabled but still has at least
     * one category still enabled.
     *
     * @return true if partially disabled, false if all categories are enabled or all
     * categories are disabled
     */
    public boolean isVeinMinerPartiallyDisabled() {
        return !isVeinMinerDisabled() && !isVeinMinerEnabled();
    }

    /**
     * Get this player's disabled {@link VeinMinerToolCategory VeinMinerToolCategories}.
     *
     * @return the disabled tool categories
     */
    @NotNull
    @UnmodifiableView
    public Set<VeinMinerToolCategory> getDisabledCategories() {
        return Collections.unmodifiableSet(disabledCategories);
    }

    /**
     * Set the {@link ActivationStrategy} to use for this player.
     *
     * @param activationStrategy the activation strategy
     */
    public void setActivationStrategy(@NotNull ActivationStrategy activationStrategy) {
        this.dirty |= (this.activationStrategy != activationStrategy);
        this.activationStrategy = activationStrategy;
    }

    /**
     * Get the {@link ActivationStrategy} to use for this player.
     *
     * @return the activation strategy
     */
    @NotNull
    public ActivationStrategy getActivationStrategy() {
        if (activationStrategy == null) {
            this.activationStrategy = VeinMinerPlugin.getInstance().getConfiguration().getDefaultActivationStrategy();
        }

        return activationStrategy;
    }

    /**
     * Set the {@link VeinMiningPattern} to use for this player.
     *
     * @param veinMiningPattern the pattern
     * @param updateClient whether or not the client should be informed of this update
     */
    public void setVeinMiningPattern(@NotNull VeinMiningPattern veinMiningPattern, boolean updateClient) {
        boolean changed = !Objects.equals(veinMiningPattern, this.veinMiningPattern);

        this.dirty |= changed;
        this.veinMiningPattern = veinMiningPattern;

        if (changed && updateClient) {
            this.sendMessage(new ClientboundSetPattern(veinMiningPattern.getKey()));
        }
    }

    /**
     * Set the {@link VeinMiningPattern} to use for this player and update the client.
     *
     * @param veinMiningPattern the pattern
     */
    public void setVeinMiningPattern(@NotNull VeinMiningPattern veinMiningPattern) {
        this.setVeinMiningPattern(veinMiningPattern, true);
    }

    /**
     * Get the {@link VeinMiningPattern} to use for this player.
     *
     * @return the pattern
     */
    @NotNull
    public VeinMiningPattern getVeinMiningPattern() {
        if (veinMiningPattern == null) {
            this.veinMiningPattern = VeinMinerPlugin.getInstance().getConfiguration().getDefaultVeinMiningPattern();
        }

        return veinMiningPattern;
    }

    /**
     * Execute the given {@link Runnable} when the client is ready.
     *
     * @param runnable the runnable to execute
     *
     * @return true if the client is not yet ready and the task was queued, false if the task
     * was executed immediately
     *
     * @see #isClientReady()
     */
    public boolean executeWhenClientIsReady(@NotNull Runnable runnable) {
        if (!isClientReady()) {
            this.onClientReady.add(runnable);
            return true;
        }

        // If the client is ready, we might as well just execute it now
        runnable.run();
        return false;
    }

    /**
     * Execute the given {@link Consumer} when the client is ready.
     *
     * @param consumer the consumer to execute
     *
     * @return true if the client is not yet ready and the task was queued, false if the task
     * was executed immediately
     *
     * @see #isClientReady()
     */
    public boolean executeWhenClientIsReady(@NotNull Consumer<VeinMinerPlayer> consumer) {
        return executeWhenClientIsReady(() -> consumer.accept(this));
    }

    /**
     * Check whether or not the client is ready to receive messages.
     * <p>
     * This method will only be true if {@link #isUsingClientMod()} is true, and if the client
     * has successfully shaken hands with the server, is capable of being sent a client message,
     * and has been synchronized with the server as per the protocol specification.
     *
     * @return true if the client is ready, false otherwise
     *
     * @see #executeWhenClientIsReady(Runnable)
     * @see #executeWhenClientIsReady(Consumer)
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
     * Check whether or not vein miner is active as a result of this user's client mod.
     *
     * @return true if active, false otherwise
     */
    public boolean isClientKeyPressed() {
        return clientKeyPressed;
    }

    /**
     * Set the {@link ClientConfig} for this player.
     *
     * @param clientConfig the client config to set
     */
    public void setClientConfig(@NotNull ClientConfig clientConfig) {
        this.clientConfig = clientConfig;

        if (usingClientMod) {
            this.sendMessage(new ClientboundSetConfig(clientConfig));
        }
    }

    /**
     * Get the {@link ClientConfig} for this player.
     * <p>
     * Note that this configuration only really applies if {@link #isUsingClientMod()} is true.
     *
     * @return the client config
     */
    @NotNull
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    /**
     * Check whether or not vein miner is currently active and ready to be used.
     * <p>
     * <strong>NOTE:</strong> Do not confuse this with {@link #isVeinMinerEnabled()}. This method
     * verifies whether or not the player has activated vein miner according to their current
     * activation strategy ({@link #getActivationStrategy()}), <strong>NOT</strong> whether they
     * have it enabled via commands.
     *
     * @return true if active, false otherwise
     */
    public boolean isVeinMinerActive() {
        return activationStrategy.test(this);
    }

    /**
     * Set whether or not the player is actively vein mining.
     * <p>
     * Not part of the public API. This method is intended for internal use only.
     *
     * @param veinMining the new vein mining state
     */
    @Internal
    public void setVeinMining(boolean veinMining) {
        this.veinMining = veinMining;
    }

    /**
     * Check whether or not the player is actively vein mining.
     *
     * @return true if using vein miner, false otherwise
     */
    public boolean isVeinMining() {
        return veinMining;
    }

    /**
     * Set whether or not this player data should be written.
     *
     * @param dirty true if dirty, false otherwise
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Check whether or not this player data has been modified since last write.
     *
     * @return true if modified, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    @Internal
    @Override
    public void sendMessage(@NotNull NamespacedKey channel, byte @NotNull [] message) {
        this.player.sendPluginMessage(VeinMinerPlugin.getInstance(), channel.toString(), message);
    }

    /**
     * Send a {@link Message} to this player across the {@link VeinMiner#PROTOCOL VeinMiner protocol}.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull Message<VeinMinerClientboundMessageListener> message) {
        VeinMiner.PROTOCOL.sendMessageToClient(this, message);
    }

    @Internal
    @Override
    public void handleHandshake(@NotNull ServerboundHandshake message) {
        int serverProtocolVersion = VeinMiner.PROTOCOL.getVersion();
        if (serverProtocolVersion != message.getProtocolVersion()) {
            this.player.kickPlayer("Your client-side version of VeinMiner (for Bukkit) is " + (serverProtocolVersion > message.getProtocolVersion() ? "out of date. Please update." : "too new. Please downgrade."));
            return;
        }

        this.usingClientMod = true;
        this.setActivationStrategy(ActivationStrategy.CLIENT);
        this.dirty = false; // We can force dirty = false. Data hasn't loaded yet, but we still want to set the strategy to client automatically

        /*
         * Let the client know whether or not the client is even allowed.
         * We send this one tick later so we know that the player's connection has been initialized
         */
        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.sendMessage(new ClientboundHandshakeResponse());

            // Synchronize all registered patterns to the client
            PatternRegistry patternRegistry = plugin.getPatternRegistry();

            List<NamespacedKey> patternKeys = new ArrayList<>();
            patternRegistry.getPatterns().forEach(pattern -> patternKeys.add(pattern.getKey()));
            VeinMiningPattern defaultPattern = plugin.getConfiguration().getDefaultVeinMiningPattern();

            // Move the default pattern to the start if it wasn't already there
            if (patternKeys.size() > 1 && patternKeys.remove(defaultPattern.getKey())) {
                patternKeys.add(0, defaultPattern.getKey());
            }

            // Don't send any patterns to which the player does not have access
            patternKeys.removeIf(patternKey -> {
                VeinMiningPattern pattern = patternRegistry.get(patternKey);
                if (pattern == null) {
                    return true;
                }

                String permission = pattern.getPermission();
                return permission != null && !player.hasPermission(permission);
            });

            this.sendMessage(new ClientboundSyncRegisteredPatterns(patternKeys));
            this.sendMessage(new ClientboundSetConfig(clientConfig));

            // The client is ready, accept post-client init tasks now
            this.clientReady = true;

            Runnable runnable;
            while ((runnable = onClientReady.poll()) != null) {
                runnable.run();
            }
        }, 1);
    }

    @Internal
    @Override
    public void handleToggleVeinMiner(@NotNull ServerboundToggleVeinMiner message) {
        if (!clientConfig.isAllowActivationKeybind()) {
            return;
        }

        if (!VMEventFactory.callPlayerClientActivateVeinMinerEvent(player, message.isActivated())) {
            return;
        }

        this.clientKeyPressed = message.isActivated();
    }

    @Internal
    @Override
    public void handleRequestVeinMine(@NotNull ServerboundRequestVeinMine message) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(itemStack);

        if (category == null) {
            this.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        RayTraceResult rayTraceResult = player.rayTraceBlocks(6);
        if (rayTraceResult == null) {
            this.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        Block targetBlock = rayTraceResult.getHitBlock();
        BlockFace targetBlockFace = rayTraceResult.getHitBlockFace();

        if (targetBlock == null || targetBlockFace == null) {
            this.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        // Validate the client's target block against the server's client block. It should be within 2 blocks of the client's target
        BlockPosition clientTargetBlock = message.getPosition();
        if (clientTargetBlock.distanceSquared(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()) >= 4) { // TODO: Reach attribute
            this.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        targetBlock = targetBlock.getWorld().getBlockAt(clientTargetBlock.x(), clientTargetBlock.y(), clientTargetBlock.z());
        BlockData targetBlockData = targetBlock.getBlockData();

        VeinMinerManager veinMinerManager = plugin.getVeinMinerManager();
        VeinMinerBlock vmBlock = veinMinerManager.getVeinMinerBlock(targetBlockData, category);

        if (vmBlock == null) {
            this.sendMessage(new ClientboundVeinMineResults());
            return;
        }

        BlockList aliasBlockList = veinMinerManager.getAlias(vmBlock);
        List<Block> blocks = getVeinMiningPattern().allocateBlocks(targetBlock, targetBlockFace, vmBlock, category.getConfig(), aliasBlockList);

        this.sendMessage(new ClientboundVeinMineResults(blocks.parallelStream().map(block -> new BlockPosition(block.getX(), block.getY(), block.getZ())).toList()));
    }

    @Internal
    @Override
    public void handleSelectPattern(@NotNull ServerboundSelectPattern message) {
        if (!clientConfig.isAllowPatternSwitchingKeybind()) {
            return;
        }

        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        VeinMiningPattern pattern = plugin.getPatternRegistry().getOrDefault(message.getPatternKey(), plugin.getConfiguration().getDefaultVeinMiningPattern());
        String patternPermission = pattern.getPermission();

        if (patternPermission != null && !player.hasPermission(patternPermission)) {
            return;
        }

        PlayerVeinMiningPatternChangeEvent event = VMEventFactory.callPlayerVeinMiningPatternChangeEvent(player, getVeinMiningPattern(), pattern, PlayerVeinMiningPatternChangeEvent.Cause.CLIENT);
        if (event.isCancelled()) {
            return;
        }

        this.setVeinMiningPattern(event.getNewPattern());
    }

}
