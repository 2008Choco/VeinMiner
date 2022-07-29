package wtf.choco.veinminer.platform;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitPlatformCommandSender implements PlatformCommandSender {

    private final CommandSender sender;

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

}
