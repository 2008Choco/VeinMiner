package wtf.choco.veinminer.listener;

import java.util.List;
import java.util.Set;

import com.google.common.base.Enums;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.api.event.PlayerVeinMineEvent;
import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.VMPlayerData;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplate;
import wtf.choco.veinminer.utils.ItemValidator;
import wtf.choco.veinminer.utils.NonNullHashSet;
import wtf.choco.veinminer.utils.Pair;
import wtf.choco.veinminer.utils.ReflectionUtil;
import wtf.choco.veinminer.utils.metrics.StatTracker;

public final class BreakBlockListener implements Listener {

    private final Set<Block> blocks = new NonNullHashSet<>();
    private final StatTracker statTracker = StatTracker.get();

    private final VeinMiner plugin;
    private final VeinMinerManager manager;

    public BreakBlockListener(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.manager = plugin.getVeinMinerManager();
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getClass() != BlockBreakEvent.class) return; // For plugins such as McMMO, who fire custom events

        Block origin = event.getBlock();
        if (blocks.contains(origin)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        Pair<ToolCategory, ToolTemplate> categoryTemplatePair = ToolCategory.getWithTemplate(item);
        ToolCategory category = categoryTemplatePair.getLeft();
        ToolTemplate toolTemplate = categoryTemplatePair.getRight();
        if (category == null || (category != ToolCategory.HAND && toolTemplate == null)) return;

        // Invalid player state check
        ActivationStrategy activation = Enums.getIfPresent(ActivationStrategy.class, plugin.getConfig().getString("ActivationStrategy", "SNEAK")).or(ActivationStrategy.SNEAK);
        AlgorithmConfig algorithmConfig = (toolTemplate != null) ? toolTemplate.getConfig() : category.getConfig();
        VMPlayerData playerData = VMPlayerData.get(player);
        if (!activation.isValid(player) || algorithmConfig.isDisabledWorld(origin.getWorld())
                || player.getGameMode() != GameMode.SURVIVAL
                || !player.hasPermission("veinminer.veinmine." + category.getId().toLowerCase())
                || playerData.isVeinMinerDisabled(category) || !ItemValidator.isValid(item, category)) {
            return;
        }

        BlockData originBlockData = origin.getBlockData();
        if (!manager.isVeinMineable(originBlockData, category)) return;

        // TIME TO VEINMINE
        this.blocks.add(origin);
        VeinBlock originVeinBlock = manager.getVeinBlockFromBlockList(originBlockData, category);
        if (originVeinBlock == null) return;

        VeinMiningPattern pattern = playerData.getPattern();
        pattern.allocateBlocks(blocks, originVeinBlock, origin, category, toolTemplate, algorithmConfig, manager.getAliasFor(origin));
        this.blocks.removeIf(Block::isEmpty);

        // Fire a new PlayerVeinMineEvent
        PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, originVeinBlock, category, blocks, pattern);
        Bukkit.getPluginManager().callEvent(vmEvent);
        if (vmEvent.isCancelled()) {
            this.blocks.clear();
            return;
        }

        // Anticheat support
        List<AntiCheatHook> hooks = plugin.getAnticheatHooks();
        hooks.forEach(h -> h.exempt(player));

        // Actually destroying the allocated blocks
        int maxDurability = item.getType().getMaxDurability() - (plugin.getConfig().getBoolean("RepairFriendlyVeinMiner", false) ? 1 : 0);
        boolean hasDurability = (maxDurability > 0);
        for (Block block : blocks) {
            if (hasDurability && category != ToolCategory.HAND) {
                if (ItemValidator.isEmpty(item)) break;

                ItemMeta meta = item.getItemMeta();
                if (meta == null || ((Damageable) meta).getDamage() >= maxDurability) break;
            }

            Material currentType = block.getType();
            if (ReflectionUtil.breakBlock(player, block)) {
                this.statTracker.accumulateVeinMinedMaterial(currentType);
            }
        }

        this.blocks.clear();

        // VEINMINER - DONE

        // Unexempt from anticheats
        hooks.stream().filter(h -> h.shouldUnexempt(player)).forEach(h -> h.unexempt(player));
    }

}
