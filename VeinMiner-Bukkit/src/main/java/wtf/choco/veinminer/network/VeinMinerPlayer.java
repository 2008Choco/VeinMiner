package wtf.choco.veinminer.network;

import com.google.common.base.Preconditions;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.NamespacedKey;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

public final class VeinMinerPlayer implements MessageReceiver, ServerboundPluginMessageListener {

    private ActivationStrategy activationStrategy = ActivationStrategy.getDefaultActivationStrategy();
    private final Set<VeinMinerToolCategory> disabledCategories = new HashSet<>();
    private VeinMiningPattern veinMiningPattern;

    private boolean dirty = false;

    private boolean usingClientMod = false;
    private boolean veinMinerActive = false; // Not to be confused with "enabled". Active means their activation strategy is enabled. Used for the client

    private final Reference<Player> player;
    private final UUID playerUUID;

    public VeinMinerPlayer(@NotNull Player player) {
        this.player = new WeakReference<>(player);
        this.playerUUID = player.getUniqueId();
    }

    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Nullable
    public Player getPlayer() {
        return player.get();
    }

    /**
     * Enable VeinMiner for this player (all categories).
     */
    public void enableVeinMiner() {
        this.dirty = !disabledCategories.isEmpty();
        this.disabledCategories.clear();
    }

    /**
     * Enable VeinMiner for this player for a specific category.
     *
     * @param category the category to enable
     */
    public void enableVeinMiner(@NotNull VeinMinerToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot enable null category");
        this.dirty = disabledCategories.remove(category);
    }

    /**
     * Disable VeinMiner for this player (all categories).
     */
    public void disableVeinMiner() {
        this.dirty = disabledCategories.addAll(VeinMinerPlugin.getInstance().getToolCategoryRegistry().getAll());
    }

    /**
     * Disable VeinMiner for this player for a specific category.
     *
     * @param category the category to disable
     */
    public void disableVeinMiner(@NotNull VeinMinerToolCategory category) {
        Preconditions.checkArgument(category != null, "Cannot disable null category");
        this.dirty = disabledCategories.add(category);
    }

    /**
     * Set VeinMiner's enabled state for this player (all categories).
     *
     * @param enable whether or not to enable VeinMiner
     */
    public void setVeinMinerEnabled(boolean enable) {
        if (enable) {
            this.enableVeinMiner();
        } else {
            this.disableVeinMiner();
        }
    }

    /**
     * Set VeinMiner's enabled state for this player for a specific category.
     *
     * @param enable whether or not to enable VeinMiner
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
     * Check whether or not VeinMiner is enabled for this player (at least one category).
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVeinMinerEnabled() {
        return disabledCategories.isEmpty();
    }

    /**
     * Check whether or not VeinMiner is enabled for this player for the specified category.
     *
     * @param category the category to check
     *
     * @return true if enabled, false otherwise
     */
    public boolean isVeinMinerEnabled(@NotNull VeinMinerToolCategory category) {
        return !disabledCategories.contains(category);
    }

    /**
     * Check whether or not VeinMiner is disabled for this player (all categories)
     *
     * @return true if disabled, false otherwise
     */
    public boolean isVeinMinerDisabled() {
        return disabledCategories.size() >= VeinMinerPlugin.getInstance().getToolCategoryRegistry().size();
    }

    /**
     * Check whether or not VeinMiner is disabled for this player for the specified category.
     *
     * @param category the category to check
     *
     * @return true if disabled, false otherwise
     */
    public boolean isVeinMinerDisabled(@NotNull VeinMinerToolCategory category) {
        return disabledCategories.contains(category);
    }

    /**
     * Check whether or not VeinMiner is disabled in at least one category. This is effectively
     * the inverse of {@link #isVeinMinerEnabled()}.
     *
     * @return true if at least one category is disabled, false otherwise (all enabled)
     */
    public boolean isVeinMinerPartiallyDisabled() {
        return !disabledCategories.isEmpty();
    }

    /**
     * Set the activation strategy to use for this player.
     *
     * @param activationStrategy the activation strategy
     */
    public void setActivationStrategy(@NotNull ActivationStrategy activationStrategy) {
        Preconditions.checkArgument(activationStrategy != null, "activationStrategy must not be null");

        this.dirty = (this.activationStrategy != activationStrategy);
        this.activationStrategy = activationStrategy;
    }

    /**
     * Get the activation strategy to use for this player.
     *
     * @return the activation strategy
     */
    @NotNull
    public ActivationStrategy getActivationStrategy() {
        return activationStrategy;
    }

    public void setVeinMiningPattern(@NotNull VeinMiningPattern veinMiningPattern) {
        this.veinMiningPattern = veinMiningPattern;
    }

    @NotNull
    public VeinMiningPattern getVeinMiningPattern() {
        if (veinMiningPattern == null) {
            this.veinMiningPattern = VeinMinerPlugin.getInstance().getDefaultVeinMiningPattern();
        }

        return veinMiningPattern;
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
     * Check whether or not VeinMiner is active as a result of this user's client mod.
     * <p>
     * <strong>NOTE:</strong> Do not confuse this with {@link #isVeinMinerEnabled()}. This method
     * verifies whether or not the player has activated VeinMiner using the client-sided mod with
     * a key, <strong>NOT</strong> whether or not they have VeinMiner enabled through commands.
     *
     * @return true if active, false otherwise
     */
    public boolean isVeinMinerActive() {
        return veinMinerActive;
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
    public void handleServerboundHandshake(@NotNull PluginMessageServerboundHandshake message) {
        Player player = getPlayer();
        assert player != null;

        int serverProtocolVersion = VeinMiner.PROTOCOL.getVersion();
        if (serverProtocolVersion != message.getProtocolVersion()) {
            player.kickPlayer("Your client-side version of VeinMiner (for Bukkit) is " + (serverProtocolVersion > message.getProtocolVersion() ? "out of date. Please update." : "too new. Please downgrade."));
        }

        FileConfiguration config = VeinMinerPlugin.getInstance().getConfig();
        if (!config.getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
            List<String> disallowedMessage = config.getStringList(VMConstants.CONFIG_CLIENT_DISALLOWED_MESSAGE);
            disallowedMessage.forEach(line -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', line)));
            return;
        }

        this.usingClientMod = true;
        this.setActivationStrategy(ActivationStrategy.CLIENT);
        this.dirty = false; // We can force dirty = false. Data hasn't loaded yet, but we still want to set the strategy to client automatically
    }

    @Internal
    @Override
    public void handleServerboundToggleVeinMiner(@NotNull PluginMessageServerboundToggleVeinMiner message) {
        Player player = getPlayer();
        assert player != null;

        if (!VeinMinerPlugin.getInstance().getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
            return;
        }

        if (!VMEventFactory.handlePlayerClientActivateVeinMinerEvent(player, message.isActivated())) {
            return;
        }

        this.veinMinerActive = message.isActivated();
    }

}
