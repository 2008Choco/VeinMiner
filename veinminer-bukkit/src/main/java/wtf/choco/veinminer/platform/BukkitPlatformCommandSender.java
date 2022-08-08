package wtf.choco.veinminer.platform;

import java.util.Objects;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit implementation of {@link PlatformCommandSender}.
 */
public class BukkitPlatformCommandSender implements PlatformCommandSender {

    private final CommandSender sender;

    /**
     * Construct a new {@link BukkitPlatformCommandSender}.
     *
     * @param sender the bukkit {@link CommandSender}
     */
    public BukkitPlatformCommandSender(@NotNull CommandSender sender) {
        this.sender = sender;
    }

    @NotNull
    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.sender.sendMessage(message);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public int hashCode() {
        return sender.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitPlatformCommandSender other && Objects.equals(sender, other.sender));
    }

}
