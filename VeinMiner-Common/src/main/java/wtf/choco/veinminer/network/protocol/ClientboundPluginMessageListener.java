package wtf.choco.veinminer.network.protocol;

import wtf.choco.veinminer.network.PluginMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;

/**
 * VeinMiner's client bound plugin message listener.
 * <p>
 * Methods in this class are intentionally undocumented as they are mostly self-explanatory.
 */
public interface ClientboundPluginMessageListener extends PluginMessageListener {

    public void handleClientboundHandshakeResponse(PluginMessageClientboundHandshakeResponse message);

}
