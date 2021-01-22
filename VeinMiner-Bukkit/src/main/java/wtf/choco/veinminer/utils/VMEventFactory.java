package wtf.choco.veinminer.utils;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.api.event.PlayerClientActivateVeinMinerEvent;
import wtf.choco.veinminer.api.event.PlayerVeinMineEvent;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;

/**
 * A collection of factory methods for VeinMiner events.
 */
public final class VMEventFactory {

    private VMEventFactory() { }

    /**
     * Call the {@link PlayerVeinMineEvent}.
     *
     * @param player the player
     * @param type the type of block being vein mined
     * @param item the item used to vein mine
     * @param category the tool category
     * @param blocks the blocks being vein mined
     * @param pattern the pattern being used to vein mine
     *
     * @return the event
     */
    public static PlayerVeinMineEvent callPlayerVeinMineEvent(@NotNull Player player, @NotNull VeinBlock type, @Nullable ItemStack item, @NotNull ToolCategory category, @NotNull Set<@NotNull Block> blocks, @NotNull VeinMiningPattern pattern) {
        PlayerVeinMineEvent event = new PlayerVeinMineEvent(player, type, item, category, blocks, pattern);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Call the {@link PlayerClientActivateVeinMinerEvent}.
     *
     * @param player the player
     * @param activated the new activation state
     *
     * @return true if not cancelled, false if cancelled
     */
    public static boolean handlePlayerClientActivateVeinMinerEvent(@NotNull Player player, boolean activated) {
        PlayerClientActivateVeinMinerEvent event = new PlayerClientActivateVeinMinerEvent(player, activated);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

}
