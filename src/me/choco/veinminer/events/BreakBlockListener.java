package me.choco.veinminer.events;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.PlayerVeinMineEvent;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.utils.ConfigOption;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.versions.VersionBreaker;

public class BreakBlockListener implements Listener{
	
	private static final int MAX_ITERATIONS = 15;
	private Set<Block> blocks = Sets.newHashSet(), blocksToAdd = Sets.newHashSet();

	private VeinMiner plugin;
	private VeinMinerManager manager;
	private VersionBreaker breaker;
	public BreakBlockListener(VeinMiner plugin){
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
		this.breaker = plugin.getVersionBreaker();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onBlockBreak(BlockBreakEvent event){
		if (!event.getClass().equals(BlockBreakEvent.class)) return;
		if (blocks.contains(event.getBlock())) return;
		Block block = event.getBlock();
		
		Player player = event.getPlayer();
		if (breaker.getItemInHand(player) == null) return;
		
		// VeinTool used check
		ItemStack itemUsed = breaker.getItemInHand(player);
		VeinTool usedTool = VeinTool.fromMaterial(itemUsed.getType());
		if (usedTool == null) usedTool = VeinTool.ALL;
		
		// Invalid player state check
		if (manager.isDisabledInWorld(block.getWorld())) return;
		if ((player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) return;
		if (!player.hasPermission("veinminer.veinmine." + usedTool.getName().toLowerCase())) return;
		if ((!manager.isVeinable(usedTool, block.getType(), block.getData()) 
				&& !(manager.isVeinable(VeinTool.ALL, block.getType(), block.getData()) && player.hasPermission("veinminer.veinmine.all")))) return;
		if (manager.hasVeinMinerDisabled(player, usedTool)) return;
		if (!canActivate(player)) return;
		if (block.getDrops(itemUsed).isEmpty()) return;
		
		// TIME TO VEINMINE
		blocks.add(block);
		int maxVeinSize = usedTool.getMaxVeinSize();
		
		// New VeinMiner algorithm- Allocate blocks to break
		for (int i = 0; i < MAX_ITERATIONS; i++){
			Iterator<Block> trackedBlocks = blocks.iterator();
			while (trackedBlocks.hasNext() && blocks.size() + blocksToAdd.size() <= maxVeinSize){
				Block b = trackedBlocks.next();
				for (BlockFace face : ConfigOption.FACES_TO_MINE){
					if (blocks.size() + blocksToAdd.size() >= maxVeinSize) break;
					
					Block nextBlock = b.getRelative(face);
					if (!blockIsSameMaterial(block, nextBlock)
							|| blocks.contains(nextBlock)) continue;
					blocksToAdd.add(nextBlock);
				}
			}
			
			blocks.addAll(blocksToAdd);
			blocksToAdd.clear();
			
			if (blocks.size() >= maxVeinSize) break;
		}
		
		// Fire a new PlayerVeinMineEvent
		PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, new VeinBlock(block.getType(), block.getData()), blocks);
		Bukkit.getPluginManager().callEvent(vmEvent);
		if (vmEvent.isCancelled()){
			this.blocks.clear();
			return;
		}
		blocks = vmEvent.getBlocks(); //Just in case it's modified in the event
		
		/* Anti Cheat support start */
		boolean unexemptNCP = false;
		if (plugin.isNCPEnabled()){
			if (!NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK)){
				NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK);
				unexemptNCP = true;
			}
		}
		if (plugin.isAACEnabled())
			plugin.getAntiCheatSupport().exemptFromViolation(player);
		/* Anti Cheat support end */
		
		// Actually destroying the allocated blocks
		boolean usesDurability = usedTool.usesDurability();
		int maxDurability = itemUsed.getType().getMaxDurability() - (ConfigOption.REPAIR_FRIENDLY_VEINMINER ? 1 : 0);
		for (Block b : blocks){
			short priorDurability = itemUsed.getDurability();
			breaker.breakBlock(player, b);
			short newDurability = itemUsed.getDurability();
			
			// Unbreaking enchantment precaution
			if (!usesDurability && priorDurability < newDurability)
				itemUsed.setDurability((short) (newDurability - 1));
			
			// Durability check
			if (newDurability >= maxDurability) break;
		}
		
		this.blocks.clear();
		
		// VEINMINER - DONE
		
		/* Anti Cheat Support ... Check if need to unexempt, in case they had been exempted prior to VeinMining */
		if (plugin.isNCPEnabled())
			if (unexemptNCP) NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK);
		if (plugin.isAACEnabled())
			plugin.getAntiCheatSupport().unexemptFromViolation(player);
	}
	
	@SuppressWarnings("deprecation")
	private boolean blockIsSameMaterial(Block original, Block block) {
		Material originalType = original.getType(), blockType = block.getType();
		if (blockType.equals(Material.GLOWING_REDSTONE_ORE) || blockType.equals(Material.REDSTONE_ORE)){
			if ((blockType.equals(originalType))
					|| (blockType.equals(Material.REDSTONE_ORE) && originalType.equals(Material.GLOWING_REDSTONE_ORE))
					|| (blockType.equals(Material.GLOWING_REDSTONE_ORE) && originalType.equals(Material.REDSTONE_ORE))){
				return true;
			}
		}
		return (blockType.equals(originalType) && block.getData() == original.getData());
	}
	
	private boolean canActivate(Player player){
		String mode = ConfigOption.ACTIVATION_MODE;
		return (
				(mode.equalsIgnoreCase("SNEAK") && player.isSneaking()) ||
				(mode.equalsIgnoreCase("STAND") && !player.isSneaking())
			);
	}
}