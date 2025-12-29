package wtf.choco.veinminer.util;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class AttributeUtil {

    private AttributeUtil() { }

    public static double getReachDistance(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (attribute != null) {
            return attribute.getValue();
        }

        double reachDistance = 4.5;
        if (player.getGameMode() == GameMode.CREATIVE) {
            reachDistance += 1;
        }

        return reachDistance;
    }

}
