package wtf.choco.veinminer.network;

import com.google.common.base.Preconditions;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.event.player.PlayerVeinMiningPatternChangeEvent;
import wtf.choco.veinminer.block.BlockAccessor;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.BukkitBlockAccessor;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.manager.VeinMinerPlayerManager;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundVeinMineResults;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.pattern.PatternRegistry;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.platform.BukkitBlockState;
import wtf.choco.veinminer.platform.BukkitItemType;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

/**
 * A player wrapper containing player-related data for VeinMiner, as well as a network
 * handler for vein miner protocol messages.
 */
public final class VeinMinerPlayer implements MessageReceiver, ServerboundPluginMessageListener {

    private ActivationStrategy activationStrategy = VeinMiner.getInstance().getDefaultActivationStrategy();
    private final Set<VeinMinerToolCategory> disabledCategories = new HashSet<>();
    private VeinMiningPattern veinMiningPattern;

    private boolean dirty = false;

    private boolean clientReady = false;
    private Queue<Runnable> onClientReady = new ConcurrentLinkedQueue<>();

    private boolean usingClientMod = false;
    private boolean clientKeyPressed = false;

    private boolean veinMining = false;

    private final Reference<Player> player;
    private final UUID playerUUID;

    /**
     * Construct a new {@link VeinMinerPlayer}.
     * <p>
     * This is an internal method. To get an instance of VeinMinerPlayer, the {@link VeinMinerPlayerManager}
     * should be used instead. Constructing a new instance of this class may have unintended side-effects
     * and will not have accurate information tracked by VeinMiner.
     *
     * @param player the player
     *
     * @see VeinMinerPlayerManager
     */
    @Internal
    public VeinMinerPlayer(@NotNull Player player) {
        this.player = new WeakReference<>(player);
        this.playerUUID = player.getUniqueId();

        // Assign metadata values to this player
        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();
        player.setMetadata(VMConstants.METADATA_KEY_VEINMINING, new LazyMetadataValue(plugin, CacheStrategy.NEVER_CACHE, this::isVeinMining));
        player.setMetadata(VMConstants.METADATA_KEY_VEIN_MINER_ACTIVE, new LazyMetadataValue(plugin, CacheStrategy.NEVER_CACHE, this::isVeinMinerActive));
    }

    /**
     * Get the {@link UUID} of this player.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Get the {@link Player} instance of this player (if still valid).
     *
     * @return the player instance, or null if no longer available
     */
    @Nullable
    public Player getPlayer() {
        return player.get();
    }

    /**
     * Enable vein miner for this player (all categories).
     */
    public void enableVeinMiner() {
        this.dirty |= !disabledCategories.isEmpty();
        this.disabledCategories.clear();
    }

    /**
     * Enable vein miner for this player for a specific category.
     *
     * @param category the category to enable
     */
    public void enableVeinMiner(@NotNull VeinMinerToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot enable null category");
        this.dirty |= disabledCategories.remove(category);
    }

    /**
     * Disable vein miner for this player (all categories).
     */
    public void disableVeinMiner() {
        this.dirty |= disabledCategories.addAll(VeinMinerPlugin.getInstance().getToolCategoryRegistry().getAll());
    }

    /**
     * Disable vein miner for this player for a specific category.
     *
     * @param category the category to disable
     */
    public void disableVeinMiner(@NotNull VeinMinerToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot disable null category");
        this.dirty |= disabledCategories.add(category);
    }

    /**
     * Set vein miner's enabled state for this player (all categories).
     *
     * @param enable whether or not to enable vein miner
     */
    public void setVeinMinerEnabled(boolean enable) {
        if (enable) {
            this.enableVeinMiner();
        } else {
            this.disableVeinMiner();
        }
    }

    /**
     * Set vein miner's enabled state for this player for a specific category.
     *
     * @param enable whether or not to enable vein miner
     * @param category the category to enable (or disable)
     */
    public void setVeinMinerEnabled(boolean enable, @NotNull VeinMinerToolCategory category) {
        if (enable) {
            this.enableVeinMiner(category);
        } else {
            this.disableVeinMiner(category);
        }
    }

    /**
     * Check whether or not vein miner is enabled for this player (at least one category).
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVeinMinerEnabled() {
        return disabledCategories.isEmpty();
    }

    /**
     * Check whether or not vein miner is enabled for this player for the specified category.
     *
     * @param category the category to check
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVeinMinerEnabled(@NotNull VeinMinerToolCategory category) {
        return !disabledCategories.contains(category);
    }

    /**
     * Check whether or not vein miner is disabled for this player (all categories)
     *
     * @return true if disabled, false otherwise
     */
    public boolean isVeinMinerDisabled() {
        return disabledCategories.size() >= VeinMinerPlugin.getInstance().getToolCategoryRegistry().size();
    }

    /**
     * Check whether or not vein miner is disabled for this player for the specified category.
     *
     * @param category the category to check
     *
     * @return true if disabled, false otherwise
     */
    public boolean isVeinMinerDisabled(@NotNull VeinMinerToolCategory category) {
        return disabledCategories.contains(category);
    }

    /**
     * Check whether or not vein miner is disabled in at least one category. This is effectively
     * the inverse of {@link #isVeinMinerEnabled()}.
     *
     * @return true if at least one category is disabled, false otherwise (all enabled)
     */
    public boolean isVeinMinerPartiallyDisabled() {
        return !disabledCategories.isEmpty();
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
        Preconditions.checkArgument(activationStrategy != null, "activationStrategy must not be null");

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
        return activationStrategy;
    }

    /**
     * Set the {@link VeinMiningPattern} to use for this player.
     *
     * @param veinMiningPattern the pattern
     * @param updateClient whether or not the client should be informed of this update
     */
    public void setVeinMiningPattern(@NotNull VeinMiningPattern veinMiningPattern, boolean updateClient) {
        boolean newPattern = !Objects.equals(veinMiningPattern, this.veinMiningPattern);

        this.dirty |= newPattern;
        this.veinMiningPattern = veinMiningPattern;

        if (newPattern && updateClient) {
            VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundSetPattern(veinMiningPattern));
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
            this.veinMiningPattern = VeinMinerPlugin.getInstance().getDefaultVeinMiningPattern();
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
        return switch (activationStrategy) {
            case ALWAYS -> true;
            case CLIENT -> clientKeyPressed;
            case SNEAK -> player.get().isSneaking();
            case STAND -> !player.get().isSneaking();
            default -> false;
        };
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
    public void sendMessage(@NotNull NamespacedKey channel, byte[] message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.sendPluginMessage(VeinMinerPlugin.getInstance(), channel.toString(), message);
    }

    @Internal
    @Override
    public void handleHandshake(@NotNull PluginMessageServerboundHandshake message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        int serverProtocolVersion = VeinMiner.PROTOCOL.getVersion();
        if (serverProtocolVersion != message.getProtocolVersion()) {
            player.kickPlayer("Your client-side version of VeinMiner (for Bukkit) is " + (serverProtocolVersion > message.getProtocolVersion() ? "out of date. Please update." : "too new. Please downgrade."));
            return;
        }

        FileConfiguration config = VeinMinerPlugin.getInstance().getConfig();
        boolean allowClientActivation = config.getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true);

        if (!allowClientActivation) {
            List<String> disallowedMessage = config.getStringList(VMConstants.CONFIG_CLIENT_DISALLOWED_MESSAGE);
            disallowedMessage.forEach(line -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', line)));
            return;
        }

        this.usingClientMod = true;
        this.setActivationStrategy(ActivationStrategy.CLIENT);
        this.dirty = false; // We can force dirty = false. Data hasn't loaded yet, but we still want to set the strategy to client automatically

        /*
         * Let the client know whether or not the client is even allowed.
         * We send this one tick later so we know that the player's connection has been initialized
         */
        Bukkit.getScheduler().runTaskLater(VeinMinerPlugin.getInstance(), () -> {
            VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundHandshakeResponse(allowClientActivation));

            // Synchronize all registered patterns to the client
            PatternRegistry patternRegistry = VeinMiner.getInstance().getPatternRegistry();

            List<NamespacedKey> patternKeys = new ArrayList<>();
            patternRegistry.getPatterns().forEach(pattern -> patternKeys.add(pattern.getKey()));
            VeinMiningPattern defaultPattern = VeinMinerPlugin.getInstance().getDefaultVeinMiningPattern();

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

            VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundSyncRegisteredPatterns(patternKeys));

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
    public void handleToggleVeinMiner(@NotNull PluginMessageServerboundToggleVeinMiner message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        if (!VeinMinerPlugin.getInstance().getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
            return;
        }

        if (!VMEventFactory.handlePlayerClientActivateVeinMinerEvent(player, message.isActivated())) {
            return;
        }

        this.clientKeyPressed = message.isActivated();
    }

    @Internal
    @Override
    public void handleRequestVeinMine(@NotNull PluginMessageServerboundRequestVeinMine message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        World world = player.getWorld();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        VeinMinerToolCategory category = VeinMiner.getInstance().getToolCategoryRegistry().get(BukkitItemType.of(itemStack.getType()));

        if (category == null) {
            VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundVeinMineResults());
            return;
        }

        // Check for the NBT value is one is present
        String nbtValue = category.getNBTValue();
        if (nbtValue != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && !nbtValue.equals(meta.getPersistentDataContainer().get(VMConstants.getVeinMinerNBTKey(), PersistentDataType.STRING))) {
                return;
            }
        }

        BlockAccessor blockAccessor = BukkitBlockAccessor.forWorld(world);
        RayTraceResult rayTraceResult = player.rayTraceBlocks(6, FluidCollisionMode.NEVER);
        Block targetBlock;
        BlockFace targetBlockFace;

        if (rayTraceResult == null || (targetBlock = rayTraceResult.getHitBlock()) == null || (targetBlockFace = rayTraceResult.getHitBlockFace()) == null) {
            VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundVeinMineResults());
            return;
        }

        BlockData targetBlockData = targetBlock.getBlockData();

        VeinMinerManager veinMinerManager = VeinMinerPlugin.getInstance().getVeinMinerManager();
        VeinMinerBlock block = veinMinerManager.getVeinMinerBlock(BukkitBlockState.of(targetBlockData), category);

        if (block == null) {
            VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundVeinMineResults());
            return;
        }

        BlockList aliasBlockList = veinMinerManager.getAlias(block);

        wtf.choco.veinminer.block.BlockFace vmBlockFace = wtf.choco.veinminer.block.BlockFace.valueOf(targetBlockFace.name());
        Set<BlockPosition> blocks = getVeinMiningPattern().allocateBlocks(blockAccessor, BlockPosition.at(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()), vmBlockFace, block, category.getConfig(), aliasBlockList);
        VeinMiner.PROTOCOL.sendMessageToClient(this, new PluginMessageClientboundVeinMineResults(blocks));
    }

    @Override
    public void handleSelectPattern(@NotNull PluginMessageServerboundSelectPattern message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        VeinMiningPattern pattern = VeinMiner.getInstance().getPatternRegistry().getOrDefault(message.getPatternKey(), VeinMinerPlugin.getInstance().getDefaultVeinMiningPattern());
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
