package wtf.choco.veinminer.player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.config.ClientConfig;

/**
 * A manager mapping players to {@link VeinMinerPlayer VeinMinerPlayers}.
 */
public final class VeinMinerPlayerManager {

    private final Map<UUID, VeinMinerPlayer> players = new HashMap<>();

    /**
     * Get the {@link VeinMinerPlayer} associated with the given {@link Player}.
     *
     * @param player the player
     *
     * @return the vein miner player, or null if not registered
     */
    @Nullable
    public VeinMinerPlayer get(@NotNull Player player) {
        return players.get(player.getUniqueId());
    }

    /**
     * Get the {@link VeinMinerPlayer} associated with the given {@link Player}.
     *
     * @param player the player
     * @param defaultConfigSupplier a supplier to create the default client configuration to use
     * if the player did not exist and needed to be registered
     *
     * @return the vein miner player
     */
    @NotNull
    public VeinMinerPlayer getOrRegister(@NotNull Player player, Supplier<ClientConfig> defaultConfigSupplier) {
        return players.computeIfAbsent(player.getUniqueId(), uuid -> new VeinMinerPlayer(player, defaultConfigSupplier.get()));
    }

    /**
     * Remove the {@link VeinMinerPlayer} of the player with the given UUID.
     *
     * @param playerUUID the UUID of the player to remove
     *
     * @return the removed VeinMinerPlayer instance, or null if none existed
     */
    @Nullable
    public VeinMinerPlayer remove(@NotNull UUID playerUUID) {
        return players.remove(playerUUID);
    }

    /**
     * Remove the {@link VeinMinerPlayer} of the given {@link Player}.
     *
     * @param player the player to remove
     *
     * @return the removed VeinMinerPlayer instance, or null if none existed
     */
    @Nullable
    public VeinMinerPlayer remove(@NotNull Player player) {
        return remove(player.getUniqueId());
    }

    /**
     * Remove the given {@link VeinMinerPlayer}.
     *
     * @param player the player to remove
     */
    public void remove(@NotNull VeinMinerPlayer player) {
        this.remove(player.getPlayerUUID());
    }

    /**
     * Get all {@link VeinMinerPlayer VeinMinerPlayers} managed by this manager.
     *
     * @return all registered players
     */
    @NotNull
    @UnmodifiableView
    public Collection<? extends VeinMinerPlayer> getAll() {
        return Collections.unmodifiableCollection(players.values());
    }

    /**
     * Get all {@link VeinMinerPlayer VeinMinerPlayers} using the client mod.
     *
     * @return all players using the client mod
     */
    @NotNull
    @Unmodifiable
    public Collection<? extends VeinMinerPlayer> getAllUsingClientMod() {
        return players.values().stream().filter(VeinMinerPlayer::isUsingClientMod).toList();
    }

    /**
     * Get the amount of players in this manager using the VeinMiner client mod.
     *
     * @return the amount of players using the client mod
     */
    public int getPlayerCountUsingClientMod() {
        return getAllUsingClientMod().size();
    }

    /**
     * Clear all players in this manager.
     */
    public void clear() {
        this.players.clear();
    }

}
