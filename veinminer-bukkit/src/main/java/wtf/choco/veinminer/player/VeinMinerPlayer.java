package wtf.choco.veinminer.player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.network.Message;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.network.receiver.MessageReceiver;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.NetworkUtil;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetPattern;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * A {@link Player} wrapper holding all VeinMiner-related data for an online player.
 */
public final class VeinMinerPlayer implements MessageReceiver {

    private ActivationStrategy activationStrategy;
    private final Set<VeinMinerToolCategory> disabledCategories = new HashSet<>();
    private VeinMiningPattern veinMiningPattern;

    private boolean dirty = false;

    private boolean veinMining = false;

    private ClientConfig clientConfig;

    private final Player player;
    private final PlayerNetworkListener networkListener;

    /**
     * Construct a new {@link VeinMinerPlayer}.
     * <p>
     * This is an internal constructor. To get an instance of VeinMinerPlayer, the {@link
     * VeinMinerPlayerManager} should be used instead. Constructing a new instance of this class may
     * have unintended side-effects and will not have accurate information tracked by VeinMiner.
     *
     * @param player the player to wrap
     * @param clientConfig the client configuration
     *
     * @see VeinMinerPlayerManager#get(Player)
     */
    @Internal
    public VeinMinerPlayer(@NotNull Player player, @NotNull ClientConfig clientConfig) {
        this.player = player;
        this.clientConfig = clientConfig;
        this.networkListener = new PlayerNetworkListener(this);
    }

    /**
     * Get the wrapped {@link Player}.
     * <p>
     * While this object will never be null, caution should be taken when interacting with this
     * object because there is no guarantee that this player will still be online.
     *
     * @return the player
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
     * Get the {@link VeinMinerServerboundMessageListener} for this player.
     * <p>
     * <strong>NOTE:</strong> Not part of the public API. This method is intended for internal use only.
     * Callers should <strong>NEVER</strong> invoke any methods in the returned object.
     *
     * @return the serverbound message listener
     */
    @Internal
    @NotNull
    public VeinMinerServerboundMessageListener getServerboundMessageListener() {
        return networkListener;
    }

    /**
     * Set whether or not the given {@link VeinMinerToolCategory} is enabled for this player.
     *
     * @param category the category whose state to update
     * @param enabled the enabled state to set
     *
     * @return true if the category state was changed, false if the category remains unchanged
     */
    public boolean setVeinMinerEnabled(@NotNull VeinMinerToolCategory category, boolean enabled) {
        boolean changed = (enabled) ? disabledCategories.remove(category) : disabledCategories.add(category);
        this.dirty |= changed;
        return changed;
    }

    /**
     * Set whether or not vein miner is enabled for all registered categories for this player.
     *
     * @param enabled the enabled state to set
     *
     * @return true if at least one category was changed as a result of the change
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
     * Check whether or not this player has the given {@link VeinMinerToolCategory} enabled.
     *
     * @param category the category
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVeinMinerEnabled(@NotNull VeinMinerToolCategory category) {
        return !disabledCategories.contains(category);
    }

    /**
     * Check whether or not this player has all registered categories enabled.
     * <p>
     * If at least one category is disabled (according to {@link #isVeinMinerEnabled(VeinMinerToolCategory)}),
     * this method will return {@code false}.
     *
     * @return true if all categories are enabled, false if at least one category is disabled
     */
    public boolean isVeinMinerEnabled() {
        return disabledCategories.isEmpty();
    }

    /**
     * Check whether or not this player has all registered categories disabled.
     * <p>
     * If at least one category is enabled (according to {@link #isVeinMinerEnabled(VeinMinerToolCategory)}),
     * this method will return {@code false}.
     *
     * @return true if all categories are disabled, false if at least one category is enabled
     */
    public boolean isVeinMinerDisabled() {
        return disabledCategories.size() >= VeinMinerPlugin.getInstance().getToolCategoryRegistry().size();
    }

    /**
     * Check whether or not vein miner has been partially disabled by this player. Vein miner is
     * considered partially disabled if one or more category is enabled, but not <em>all</em> categories.
     * In other words, there is a mix of both enabled and disabled categories, neither are all the same
     * state.
     *
     * @return true if partially disabled, false if categories are either all enabled or all disabled
     */
    public boolean isVeinMinerPartiallyDisabled() {
        return !isVeinMinerDisabled() && !isVeinMinerEnabled();
    }

    /**
     * Get an unmodifiable {@link Set} of all categories this player has disabled.
     *
     * @return all disabled tool categories
     */
    @NotNull
    @UnmodifiableView
    public Set<VeinMinerToolCategory> getDisabledCategories() {
        return Collections.unmodifiableSet(disabledCategories);
    }

    /**
     * Set this player's {@link ActivationStrategy}.
     *
     * @param strategy the new activation strategy
     */
    public void setActivationStrategy(@NotNull ActivationStrategy strategy) {
        this.dirty |= (this.activationStrategy != strategy);
        this.activationStrategy = strategy;
    }

    /**
     * Get this player's {@link ActivationStrategy}.
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
     * Set this player's active {@link VeinMiningPattern}.
     *
     * @param pattern the new vein mining pattern
     * @param updateClient whether or not the client should be informed of this update. If true and this
     * player has the client mod installed ({@link #isUsingClientMod()}, an update packet will be sent
     * to the client
     */
    public void setVeinMiningPattern(@NotNull VeinMiningPattern pattern, boolean updateClient) {
        boolean changed = !Objects.equals(pattern, this.veinMiningPattern);

        this.dirty |= changed;
        this.veinMiningPattern = pattern;

        if (changed && updateClient && isUsingClientMod()) {
            this.sendMessage(new ClientboundSetPattern(NetworkUtil.toNetwork(pattern.getKey())));
        }
    }

    /**
     * Set this player's active {@link VeinMiningPattern} and update the client if necessary.
     *
     * @param pattern the new vein mining pattern
     */
    public void setVeinMiningPattern(@NotNull VeinMiningPattern pattern) {
        this.setVeinMiningPattern(pattern, true);
    }

    /**
     * Get this player's active {@link VeinMiningPattern}.
     *
     * @return the active vein mining pattern
     */
    @NotNull
    public VeinMiningPattern getVeinMiningPattern() {
        if (veinMiningPattern == null) {
            this.veinMiningPattern = VeinMinerPlugin.getInstance().getConfiguration().getDefaultVeinMiningPattern();
        }

        return veinMiningPattern;
    }

    /**
     * Queue the given {@link Runnable} for execution when the client is ready, or execute it now
     * if the client is ready.
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
            this.networkListener.addOnClientReadyTask(runnable);
            return true;
        }

        // If the client is ready, we might as well just execute it now
        runnable.run();
        return false;
    }

    /**
     * Queue the given {@link Consumer} for execution when the client is ready, or execute it now
     * if the client is ready.
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
     * This method will only be true if {@link #isUsingClientMod()} is {@code true}, the client has
     * successfully shaken hands with the server, is capable of being sent a client message, and
     * has been synchronized with the server as per the protocol specification.
     *
     * @return true if the client is ready, false otherwise
     *
     * @see #executeWhenClientIsReady(Runnable)
     * @see #executeWhenClientIsReady(Consumer)
     */
    public boolean isClientReady() {
        return networkListener.isClientReady();
    }

    /**
     * Check whether or not this player is using the client mod.
     *
     * @return true if using client mod, false otherwise
     */
    public boolean isUsingClientMod() {
        return networkListener.isUsingClientMod();
    }

    /**
     * Check whether or not vein miner is active as a result of this user's client mod. This method will
     * always return {@code false} if the player {@link #isUsingClientMod() is not using the client mod}.
     *
     * @return true if active, false otherwise
     */
    public boolean isClientKeyPressed() {
        return networkListener.isClientKeyPressed();
    }

    /**
     * Set this player's {@link ClientConfig} and update the client.
     * <p>
     * <strong>NOTE:</strong> This configuration only really applies if the player {@link #isUsingClientMod()
     * is not using the client mod}.
     *
     * @param clientConfig the client config to set
     */
    public void setClientConfig(@NotNull ClientConfig clientConfig) {
        this.clientConfig = clientConfig;

        if (isUsingClientMod()) {
            this.sendMessage(new ClientboundSetConfig(clientConfig));
        }
    }

    /**
     * Get this player's {@link ClientConfig}.
     * <p>
     * <strong>NOTE:</strong> This configuration only really applies if the player {@link #isUsingClientMod()
     * is not using the client mod}.
     *
     * @return the client config
     */
    @NotNull
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    /**
     * Check whether or not this player has vein miner active and ready use.
     * <p>
     * <strong>NOTE:</strong> Do not confuse this with {@link #isVeinMinerEnabled()}. This method
     * verifies whether or not the player has activated vein miner according to their current
     * ({@link #getActivationStrategy() activation strategy}), <strong>NOT</strong> whether they
     * have it enabled via commands.
     *
     * @return true if active, false otherwise
     */
    public boolean isVeinMinerActive() {
        return getActivationStrategy().isActive(this);
    }

    /**
     * Set whether or not the player is actively vein mining.
     * <p>
     * <strong>NOTE:</strong> Not part of the public API. This method is intended for internal use only.
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
     * Set whether or not this player's data has changed since it was read from persistent storage
     * and needs to be written again.
     *
     * @param dirty true if dirty, false otherwise
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Check whether or not this player's data has changed since it was read from persistent storage
     * and needs to be written again.
     *
     * @return true if modified, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>NOTE:</strong> Not part of the public API. This method is intended for internal use only.
     */
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

}
