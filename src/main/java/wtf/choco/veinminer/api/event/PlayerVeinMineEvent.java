package wtf.choco.veinminer.api.event;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

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
	private final ToolCategory category;
	private final Set<Block> blocks;
	private final VeinMiningPattern pattern;

	public PlayerVeinMineEvent(Player who, VeinBlock type, ToolCategory category, Set<Block> blocks, VeinMiningPattern pattern) {
		super(who);

		this.type = type;
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
	public Set<Block> getBlocks() {
		return blocks;
	}

	/**
	 * Get the block type affected by the vein mine.
	 *
	 * @return the affected block
	 */
	public VeinBlock getAffectedBlock() {
		return type;
	}

	/**
	 * Get the tool used to initiate this vein mine.
	 *
	 * @return the tool used
	 */
	public ToolCategory getCategory() {
		return category;
	}

	/**
	 * Get the vein mining pattern used for this vein mine.
	 *
	 * @return the pattern used
	 */
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

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}