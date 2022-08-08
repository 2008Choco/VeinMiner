package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a platform-independent command-capable object.
 */
public interface PlatformCommandSender {

    /**
     * Get the name of this sender.
     *
     * @return the name
     */
    @NotNull
    public String getName();

    /**
     * Send a message.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull String message);

    /**
     * Check whether or not this sender has the given permission.
     *
     * @param permission the permission to check
     *
     * @return true if the sender has the given permission, false otherwise
     */
    public boolean hasPermission(@NotNull String permission);

}
