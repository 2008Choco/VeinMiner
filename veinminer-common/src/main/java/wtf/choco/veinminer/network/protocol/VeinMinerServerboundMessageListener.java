package wtf.choco.veinminer.network.protocol;

import org.jetbrains.annotations.NotNull;

import wtf.choco.network.listener.ServerboundMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundToggleVeinMiner;

/**
 * VeinMiner's server bound plugin message listener.
 * <p>
 * Methods in this class are intentionally undocumented as they are mostly self-explanatory.
 */
public interface VeinMinerServerboundMessageListener extends ServerboundMessageListener {

    /**
     * Handles the {@link ServerboundHandshake} message.
     *
     * @param message the message
     */
    public void handleHandshake(@NotNull ServerboundHandshake message);

    /**
     * Handles the {@link ServerboundToggleVeinMiner} message.
     *
     * @param message the message
     */
    public void handleToggleVeinMiner(@NotNull ServerboundToggleVeinMiner message);

    /**
     * Handles the {@link ServerboundRequestVeinMine} message.
     *
     * @param message the message
     */
    public void handleRequestVeinMine(@NotNull ServerboundRequestVeinMine message);

    /**
     * Handles the {@link ServerboundSelectPattern} message.
     *
     * @param message the message
     */
    public void handleSelectPattern(@NotNull ServerboundSelectPattern message);

}
