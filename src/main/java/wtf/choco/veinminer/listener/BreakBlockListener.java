package wtf.choco.veinminer.listener;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.api.event.PlayerVeinMineEvent;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.VMPlayerData;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.template.TemplatePrecedence;
import wtf.choco.veinminer.tool.template.TemplateValidator;
import wtf.choco.veinminer.utils.NonNullHashSet;
import wtf.choco.veinminer.utils.ReflectionUtil;
import wtf.choco.veinminer.utils.metrics.StatTracker;

public class BreakBlockListener implements Listener {

	private final Set<Block> blocks = new NonNullHashSet<>();
	private final StatTracker statTracker = StatTracker.get();

	private final VeinMiner plugin;
	private final VeinMinerManager manager;

	public BreakBlockListener(VeinMiner plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent event) {
		if (event.getClass() != BlockBreakEvent.class) return; // For plugins such as McMMO, who fire custom events
		if (blocks.contains(event.getBlock())) return;

		Block block = event.getBlock();
		if (manager.isDisabledInWorld(block.getWorld())) return;

		Player player = event.getPlayer();
		ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
		ToolCategory category = ToolCategory.fromItemStack(tool);

		// Activation check
		ActivationStrategy activation = EnumUtils.getEnum(ActivationStrategy.class, plugin.getConfig().getString("ActivationMode", "SNEAK"));
		if (activation == null) {
			activation = ActivationStrategy.SNEAK;
		}

		// Invalid player state check
		VMPlayerData playerData = VMPlayerData.get(player);
		if (!activation.isValid(player) || player.getGameMode() != GameMode.SURVIVAL) return;
		if (!player.hasPermission("veinminer.veinmine." + category.getName().toLowerCase())) return;
		if (playerData.isVeinMinerDisabled(category)) return;

		TemplateValidator templateValidator = manager.getTemplateValidator();
		if (!templateValidator.isValid(tool, category)) return;

		Material blockType = block.getType();
		BlockData blockData = block.getBlockData();
		TemplatePrecedence precedence = templateValidator.getPrecedence();
		if (precedence == TemplatePrecedence.CATEGORY_SPECIFIC && !manager.isVeinMineable(blockData, category)) return;
		else if ((precedence == TemplatePrecedence.GLOBAL || precedence == TemplatePrecedence.CATEGORY_SPECIFIC_GLOBAL_DEFAULT) && !manager.isVeinMineable(blockData)) return;

		// TIME TO VEINMINE
		MaterialAlias alias = manager.getAliasFor(blockType);
		if (alias == null) {
			alias = manager.getAliasFor(blockData);
		}

		this.blocks.add(block);
		VeinBlock type = VeinBlock.get(blockData);
		VeinMiningPattern pattern = playerData.getPattern();
		pattern.allocateBlocks(blocks, type, block, category, alias);
		this.blocks.removeIf(Block::isEmpty);

		// Fire a new PlayerVeinMineEvent
		PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, type, category, blocks, pattern);
		Bukkit.getPluginManager().callEvent(vmEvent);
		if (vmEvent.isCancelled()) {
			this.blocks.clear();
			return;
		}

		// Anticheat support
		List<AntiCheatHook> hooks = plugin.getAnticheatHooks();
		hooks.stream().filter(AntiCheatHook::isSupported).forEach(h -> h.exempt(player));

		// Actually destroying the allocated blocks
		int maxDurability = tool.getType().getMaxDurability() - (plugin.getConfig().getBoolean("RepairFriendlyVeinMiner", false) ? 1 : 0);
		for (Block b : blocks) {
			if (category != ToolCategory.HAND && (tool.getType() == Material.AIR || ((Damageable) tool.getItemMeta()).getDamage() >= maxDurability)) break;

			Material currentType = b.getType();
			if (ReflectionUtil.breakBlock(player, b)) {
				this.statTracker.accumulateVeinMinedMaterial(currentType);
			}
		}

		this.blocks.clear();

		// VEINMINER - DONE

		// Unexempt from anticheats
		hooks.stream().filter(AntiCheatHook::isSupported).filter(h -> h.shouldUnexempt(player)).forEach(h -> h.unexempt(player));
	}

}