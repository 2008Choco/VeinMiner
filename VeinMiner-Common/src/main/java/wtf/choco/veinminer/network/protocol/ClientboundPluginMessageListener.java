package wtf.choco.veinminer.network.protocol;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSetPattern;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundSyncRegisteredPatterns;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundVeinMineResults;

/**
 * VeinMiner's client bound plugin message listener.
 * <p>
 * Methods in this class are intentionally undocumented as they are mostly self-explanatory.
 */
public interface ClientboundPluginMessageListener extends PluginMessageListener {

    public void handleHandshakeResponse(@NotNull PluginMessageClientboundHandshakeResponse message);

    public void handleVeinMineResults(@NotNull PluginMessageClientboundVeinMineResults message);

    public void handleSyncRegisteredPatterns(@NotNull PluginMessageClientboundSyncRegisteredPatterns message);

    public void handleSetPattern(@NotNull PluginMessageClientboundSetPattern message);

}
