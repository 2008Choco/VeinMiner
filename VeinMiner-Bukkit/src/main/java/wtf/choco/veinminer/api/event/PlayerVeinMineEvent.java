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

import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * Called when VeinMiner is used for a set of blocks.
 */
public class PlayerVeinMineEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final VeinMinerBlock block;
    private final ItemStack itemStack;
    private final VeinMinerToolCategory category;
    private final Set<Block> blocks;
    private final VeinMiningPattern pattern;

    public PlayerVeinMineEvent(@NotNull Player player, @NotNull VeinMinerBlock block, @Nullable ItemStack itemStack, @NotNull VeinMinerToolCategory category, @NotNull Set<Block> blocks, @NotNull VeinMiningPattern pattern) {
        super(player);

        this.block = block;
        this.itemStack = itemStack;
        this.category = category;
        this.blocks = blocks;
        this.pattern = pattern;
    }

    /**
     * Get the original {@link VeinMinerBlock} broken in this event.
     *
     * @return the block
     */
    @NotNull
    public VeinMinerBlock getBlock() {
        return block;
    }

    /**
     * Get the item used to vein mine (if any). Any changes made to the ItemStack returned by
     * this method will not be reflected in the player's inventory.
     *
     * @return the item used to vein mine. null if none
     */
    @Nullable
    public ItemStack getItem() {
        return itemStack.clone();
    }

    /**
     * Get the category used for this vein mine.
     *
     * @return the category
     */
    @NotNull
    public VeinMinerToolCategory getCategory() {
        return category;
    }

    /**
     * Get a set of all blocks destroyed by this vein mine. This set is mutable. Modifications
     * will directly manipulate what blocks are and are not destroyed.
     *
     * @return the blocks to be affected by this event
     */
    @NotNull
    public Set<Block> getBlocks() {
        return blocks;
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
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
