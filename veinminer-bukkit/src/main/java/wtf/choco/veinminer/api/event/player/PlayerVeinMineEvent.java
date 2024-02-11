package wtf.choco.veinminer.api.event.player;

import java.util.List;

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
 * Called when a player uses vein miner.
 */
public class PlayerVeinMineEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final Block block;
    private final VeinMinerBlock veinMinerBlock;
    private final ItemStack itemStack;
    private final VeinMinerToolCategory category;
    private final List<Block> blocks;
    private final VeinMiningPattern pattern;

    /**
     * Construct a new {@link PlayerVeinMineEvent}.
     *
     * @param player the player performing the vein mine
     * @param block the origin {@link Block} that was broken by the player
     * @param veinMinerBlock the type of {@link VeinMinerBlock} that was broken at the origin
     * @param itemStack the {@link ItemStack} used to vein mine
     * @param category the {@link VeinMinerToolCategory} of the itemStack
     * @param blocks the blocks to be destroyed as a result of vein miner
     * @param pattern the pattern used to vein mine
     */
    public PlayerVeinMineEvent(@NotNull Player player, @NotNull Block block, @NotNull VeinMinerBlock veinMinerBlock, @Nullable ItemStack itemStack, @NotNull VeinMinerToolCategory category, @NotNull List<Block> blocks, @NotNull VeinMiningPattern pattern) {
        super(player);

        this.block = block;
        this.veinMinerBlock = veinMinerBlock;
        this.itemStack = itemStack;
        this.category = category;
        this.blocks = blocks;
        this.pattern = pattern;
    }

    /**
     * Get the origin {@link Block} that was destroyed to trigger this vein mine.
     *
     * @return the origin block
     */
    @NotNull
    public Block getBlock() {
        return block;
    }

    /**
     * Get the {@link VeinMinerBlock} broken at the origin in this event.
     *
     * @return the block
     */
    @NotNull
    public VeinMinerBlock getVeinMinerBlock() {
        return veinMinerBlock;
    }

    /**
     * Get the item used to vein mine (if any). Any changes made to the {@link ItemStack}
     * returned by this method will not be reflected in the player's inventory.
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
     * Get a {@link List} of all blocks destroyed by this vein mine. This list is mutable. Changes made
     * to the returned collection will directly affect what blocks are destroyed.
     * <p>
     * Note that just because a block is present in the returned collection does not mean that it will
     * be destroyed for certain. Additional checks are made on the blocks in this collection during the
     * vein mining process including whether or not the player is allowed to break the block (e.g.
     * support for land claiming plugins or other protection plugins such as WorldGuard).
     *
     * @return the blocks to be destroyed
     */
    @NotNull
    public List<Block> getBlocks() {
        return blocks;
    }

    /**
     * Get the {@link VeinMiningPattern} used for this vein mine.
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
