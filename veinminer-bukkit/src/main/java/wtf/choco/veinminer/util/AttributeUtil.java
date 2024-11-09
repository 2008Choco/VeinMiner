package wtf.choco.veinminer.util;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class AttributeUtil {

    private static final Attribute ATTRIBUTE_BLOCK_INTERACTION_RANGE = getAttribute(
            NamespacedKey.minecraft("player.block_interaction_range"),
            NamespacedKey.minecraft("block_interaction_range") // "player." prefix removed in 1.21.2
    );

    private AttributeUtil() { }

    public static double getReachDistance(Player player) {
        if (ATTRIBUTE_BLOCK_INTERACTION_RANGE != null) {
            AttributeInstance attribute = player.getAttribute(ATTRIBUTE_BLOCK_INTERACTION_RANGE);
            if (attribute != null) {
                return attribute.getValue();
            }
        }

        double reachDistance = 4.5;
        if (player.getGameMode() == GameMode.CREATIVE) {
            reachDistance += 1;
        }

        return reachDistance;
    }

    private static Attribute getAttribute(NamespacedKey... possibleKeys) {
        for (NamespacedKey key : possibleKeys) {
            Attribute attribute = Registry.ATTRIBUTE.get(key);
            if (attribute != null) {
                return attribute;
            }
        }

        return null;
    }

}
