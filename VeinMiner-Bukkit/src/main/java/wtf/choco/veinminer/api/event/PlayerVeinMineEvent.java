package wtf.choco.veinminer.api.event;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;

/**
 * Called when VeinMiner is activated for a set of blocks.
 */
public class PlayerVeinMineEvent extends PlayerEvent implements Cancellable {

    private static HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final VeinBlock type;
    private final ItemStack item;
    private final ToolCategory category;
    private final Set<@NotNull Block> blocks;
    private final VeinMiningPattern pattern;

    public PlayerVeinMineEvent(@NotNull Player player, @NotNull VeinBlock type, @Nullable ItemStack item, @NotNull ToolCategory category, @NotNull Set<@NotNull Block> blocks, @NotNull VeinMiningPattern pattern) {
        super(player);

        this.type = type;
        this.item = item;
        this.category = category;
        this.blocks = blocks;
        this.pattern = pattern;
    }

    /**
     * Get a set of all blocks destroyed by this vein mine. This set is mutable. Modifications
     * will directly manipulate what blocks are and are not destroyed.
     *
     * @return the blocks to be affected by this event
     */
    @NotNull
    public Set<@NotNull Block> getBlocks() {
        return blocks;
    }

    /**
     * Get the block type affected by the vein mine.
     *
     * @return the affected block
     */
    @NotNull
    public VeinBlock getAffectedBlock() {
        return type;
    }

    /**
     * Get the item used to vein mine (if any). Any changes made to the ItemStack returned by
     * this method will not be reflected in the player's inventory.
     *
     * @return the item used to vein mine. null if none
     */
    @Nullable
    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * Get the tool used to initiate this vein mine.
     *
     * @return the tool used
     */
    @NotNull
    public ToolCategory getCategory() {
        return category;
    }

    /**
     * Get the vein mining pattern used for this vein mine.
     *
     * @return the pattern used
     */
    @NotNull
    public VeinMiningPattern getPattern() {
        return pattern;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
