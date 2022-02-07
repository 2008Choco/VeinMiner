package wtf.choco.veinminer.network.protocol;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundSelectPattern;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;

/**
 * VeinMiner's server bound plugin message listener.
 * <p>
 * Methods in this class are intentionally undocumented as they are mostly self-explanatory.
 */
public interface ServerboundPluginMessageListener extends PluginMessageListener {

    public void handleHandshake(@NotNull PluginMessageServerboundHandshake message);

    public void handleToggleVeinMiner(@NotNull PluginMessageServerboundToggleVeinMiner message);

    public void handleRequestVeinMine(@NotNull PluginMessageServerboundRequestVeinMine message);

    public void handleSelectPattern(@NotNull PluginMessageServerboundSelectPattern message);

}
