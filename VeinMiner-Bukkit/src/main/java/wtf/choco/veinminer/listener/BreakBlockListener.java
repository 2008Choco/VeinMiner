package wtf.choco.veinminer.listener;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.api.event.PlayerVeinMineEvent;
import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.PlayerPreferences;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.economy.EconomyModifier;
import wtf.choco.veinminer.integration.WorldGuardIntegration;
import wtf.choco.veinminer.metrics.StatTracker;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplate;
import wtf.choco.veinminer.utils.ItemValidator;
import wtf.choco.veinminer.utils.NonNullHashSet;
import wtf.choco.veinminer.utils.Pair;
import wtf.choco.veinminer.utils.ReflectionUtil;
import wtf.choco.veinminer.utils.VMConstants;
import wtf.choco.veinminer.utils.VMEventFactory;

public final class BreakBlockListener implements Listener {

    private final VeinMiner plugin;
    private final VeinMinerManager manager;

    public BreakBlockListener(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.manager = plugin.getVeinMinerManager();
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getClass() != BlockBreakEvent.class) { // For plugins such as McMMO, who fire custom events
            return;
        }

        Block origin = event.getBlock();
        if (origin.hasMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        Pair<@NotNull ToolCategory, @NotNull ToolTemplate> categoryTemplatePair = ToolCategory.getWithTemplate(item);
        ToolCategory category = categoryTemplatePair.getLeft();
        ToolTemplate toolTemplate = categoryTemplatePair.getRight();
        if (category == null || (category != ToolCategory.HAND && toolTemplate == null)) {
            return;
        }

        // Invalid player state check
        PlayerPreferences playerData = PlayerPreferences.get(player);
        ActivationStrategy activation = playerData.getActivationStrategy();
        AlgorithmConfig algorithmConfig = (toolTemplate != null) ? toolTemplate.getConfig() : category.getConfig();
        if (!activation.isValid(player)
                || !category.hasPermission(player)
                || manager.isDisabledGameMode(player.getGameMode())
                || playerData.isVeinMinerDisabled(category)
                || algorithmConfig.isDisabledWorld(origin.getWorld())
                || !ItemValidator.isValid(item, category)) {
            return;
        }

        BlockData originBlockData = origin.getBlockData();
        if (!manager.isVeinMineable(originBlockData, category)) {
            return;
        }

        // WorldGuard check
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard") && !WorldGuardIntegration.queryFlagVeinMiner(origin, player)) {
            player.sendMessage(ChatColor.GRAY + "You are not allowed to vein mine in this area.");
            return;
        }

        // Economy check
        EconomyModifier economy = plugin.getEconomyModifier();
        if (economy.shouldCharge(player, algorithmConfig)) {
            if (!economy.hasSufficientBalance(player, algorithmConfig)) {
                player.sendMessage(ChatColor.GRAY + "You have insufficient funds to vein mine (Required: " + ChatColor.YELLOW + algorithmConfig.getCost() + ChatColor.GRAY + ")");
                return;
            }

            economy.charge(player, algorithmConfig);
        }

        // TIME TO VEINMINE
        Set<Block> blocks = new NonNullHashSet<>();

        blocks.add(origin);
        VeinBlock originVeinBlock = manager.getVeinBlockFromBlockList(originBlockData, category);
        if (originVeinBlock == null) {
            return;
        }

        VeinMiningPattern pattern = plugin.getVeinMiningPattern();
        pattern.allocateBlocks(blocks, originVeinBlock, origin, category, toolTemplate, algorithmConfig, manager.getAliasFor(origin));
        blocks.removeIf(Block::isEmpty);
        if (blocks.isEmpty()) {
            return;
        }

        // Fire a new PlayerVeinMineEvent
        PlayerVeinMineEvent veinmineEvent = VMEventFactory.callPlayerVeinMineEvent(player, originVeinBlock, item, category, blocks, pattern);
        if (veinmineEvent.isCancelled() || blocks.isEmpty()) {
            return;
        }

        // Apply metadata to all blocks to be vein mined and all other relevant objects/entities
        player.setMetadata(VMConstants.METADATA_KEY_VEINMINING, new FixedMetadataValue(plugin, true));
        blocks.forEach(block -> {
            block.setMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED, new FixedMetadataValue(plugin, true));
            block.setMetadata(VMConstants.METADATA_KEY_VEINMINER_SOURCE, new LazyMetadataValue(plugin, CacheStrategy.CACHE_ETERNALLY, origin::getLocation));
        });

        // Anticheat support
        List<@NotNull AntiCheatHook> hooks = plugin.getAnticheatHooks();
        hooks.forEach(h -> h.exempt(player));

        // Actually destroying the allocated blocks
        int maxDurability = item.getType().getMaxDurability() - (plugin.getConfig().getBoolean(VMConstants.CONFIG_REPAIR_FRIENDLY_VEINMINER, false) ? 1 : 0);
        float hungerModifier = ((float) Math.max((plugin.getConfig().getDouble(VMConstants.CONFIG_HUNGER_HUNGER_MODIFIER)), 0.0D)) * 0.025F;
        int minimumFoodLevel = Math.max(plugin.getConfig().getInt(VMConstants.CONFIG_HUNGER_MINIMUM_FOOD_LEVEL), 0);

        String hungryMessage = plugin.getConfig().getString(VMConstants.CONFIG_HUNGER_HUNGRY_MESSAGE, "");
        if (hungryMessage == null) {
            hungryMessage = "";
        }

        hungryMessage = ChatColor.translateAlternateColorCodes('&', hungryMessage);

        for (Block block : blocks) {
            // Apply hunger
            if (hungerModifier != 0.0 && !player.hasPermission(VMConstants.PERMISSION_FREE_HUNGER)) {
                this.applyHungerDebuff(player, hungerModifier);

                if (player.getFoodLevel() <= minimumFoodLevel) {
                    if (!hungryMessage.isEmpty()) {
                        player.sendMessage(hungryMessage);
                    }

                    break;
                }
            }

            // Check for tool damage
            if (maxDurability > 0 && category != ToolCategory.HAND) {
                if (ItemValidator.isEmpty(item)) {
                    break;
                }

                ItemMeta meta = item.getItemMeta();
                if (meta == null || ((Damageable) meta).getDamage() >= maxDurability) {
                    break;
                }
            }

            // Break the block
            Material currentType = block.getType();
            if (block == origin || ReflectionUtil.breakBlock(player, block)) {
                StatTracker.accumulateVeinMinedMaterial(currentType);
            }
        }

        // Remove applied metadata
        player.removeMetadata(VMConstants.METADATA_KEY_VEINMINING, plugin);
        blocks.forEach(block -> {
            block.removeMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED, plugin);
            block.removeMetadata(VMConstants.METADATA_KEY_VEINMINER_SOURCE, plugin);
        });

        // VEINMINER - DONE

        // Unexempt from anticheats
        hooks.stream().filter(h -> h.shouldUnexempt(player)).forEach(h -> h.unexempt(player));
    }

    // Modified version of https://github.com/portablejim/VeinMiner/blob/1.9/src/main/java/portablejim/veinminer/core/MinerInstance.java#L231-L254
    private void applyHungerDebuff(Player player, float hungerModifier) {
        int foodLevel = player.getFoodLevel();
        float saturation = player.getSaturation();
        float exhaustion = player.getExhaustion();

        exhaustion = (exhaustion + hungerModifier) % 4;
        saturation -= (int) ((exhaustion + hungerModifier) / 4);

        if (saturation < 0) {
            foodLevel += saturation;
            saturation = 0;
        }

        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);
    }

}
