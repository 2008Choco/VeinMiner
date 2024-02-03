package wtf.choco.veinminer.network.protocol;

import org.jetbrains.annotations.NotNull;

import wtf.choco.network.listener.ClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetConfig;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.ClientboundVeinMineResults;

/**
 * VeinMiner's client bound plugin message listener.
 * <p>
 * Methods in this class are intentionally undocumented as they are mostly self-explanatory.
 */
public interface VeinMinerClientboundMessageListener extends ClientboundMessageListener {

    /**
     * Handles the {@link ClientboundHandshakeResponse} message.
     *
     * @param message the message
     */
    public void handleHandshakeResponse(@NotNull ClientboundHandshakeResponse message);

    /**
     * Handles the {@link ClientboundSyncRegisteredPatterns} message.
     *
     * @param message the message
     */
    public void handleSyncRegisteredPatterns(@NotNull ClientboundSyncRegisteredPatterns message);

    /**
     * Handles the {@link ClientboundSetConfig} message.
     *
     * @param message the message
     */
    public void handleSetConfig(@NotNull ClientboundSetConfig message);

    /**
     * Handles the {@link ClientboundVeinMineResults} message.
     *
     * @param message the message
     */
    public void handleVeinMineResults(@NotNull ClientboundVeinMineResults message);

    /**
     * Handles the {@link ClientboundSetPattern} message.
     *
     * @param message the message
     */
    public void handleSetPattern(@NotNull ClientboundSetPattern message);

}
