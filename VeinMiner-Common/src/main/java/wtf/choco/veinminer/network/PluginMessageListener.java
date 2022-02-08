package wtf.choco.veinminer.network;

import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;

/**
 * A generic plugin message listener. Acts as a marker interface for message listeners.
 *
 * @see ClientboundPluginMessageListener
 * @see ServerboundPluginMessageListener
 */
public interface PluginMessageListener { } // TODO: Java 17, sealed
