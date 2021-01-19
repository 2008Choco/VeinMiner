package wtf.choco.veinminer.api;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

public class ClientActivation {

    private static final Set<UUID> CLIENT_ACTIVATED = new HashSet<>();

    public static boolean isActivatedOnClient(Player player) {
        return CLIENT_ACTIVATED.contains(player.getUniqueId());
    }

    public static void setActivatedOnClient(Player player, boolean activated) {
        if (activated) {
            CLIENT_ACTIVATED.add(player.getUniqueId());
        } else {
            CLIENT_ACTIVATED.remove(player.getUniqueId());
        }
    }

}
