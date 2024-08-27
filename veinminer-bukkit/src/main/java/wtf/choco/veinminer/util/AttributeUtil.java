package wtf.choco.veinminer.util;

import com.google.common.base.Enums;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class AttributeUtil {

    private static final Attribute ATTRIBUTE_BLOCK_INTERACTION_RANGE = Enums.getIfPresent(Attribute.class, "PLAYER_BLOCK_INTERACTION_RANGE")
            .or(Enums.getIfPresent(Attribute.class, "BLOCK_INTERACTION_RANGE")) // Renamed in 1.21.2
            .orNull();

    private AttributeUtil() { }

    public static double getReachDistance(Player player) {
        if (ATTRIBUTE_BLOCK_INTERACTION_RANGE != null) {
            AttributeInstance attribute = player.getAttribute(ATTRIBUTE_BLOCK_INTERACTION_RANGE);
            if (attribute != null) {
                return attribute.getValue();
            }
        }

        return player.getGameMode() == GameMode.CREATIVE ? 4.5 : 4;
    }

}
