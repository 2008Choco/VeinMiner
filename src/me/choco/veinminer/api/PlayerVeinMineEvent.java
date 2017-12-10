package me.choco.veinminer.api;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;

/**
 * Called when VeinMiner is activated for a specific set of blocks
 */
public class PlayerVeinMineEvent extends PlayerEvent implements Cancellable {
	
	private static HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	
	private final VeinBlock type;
	private final VeinTool tool;
	private final List<Block> blocks;
	
	public PlayerVeinMineEvent(Player who, VeinBlock type, VeinTool tool, List<Block> blocks) {
		super(who);
		
		this.type = type;
		this.tool = tool;
		this.blocks = blocks;
	}
	
	/**
	 * Get a list of all blocks affected by this event. This list is mutable, you are able to modify
	 * it and manipulate what blocks are and are not modified
	 * 
	 * @return the blocks to be affected by this event
	 */
	public List<Block> getBlocks() {
		return blocks;
	}
	
	/**
	 * Get the block affected by the VeinMine
	 * 
	 * @return the block affected
	 */
	public VeinBlock getAffectedBlock() {
		return type;
	}
	
	/**
	 * Get the tool used to initiate this VeinMine
	 * 
	 * @return the tool used
	 */
	public VeinTool getTool() {
		return tool;
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