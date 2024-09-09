package wtf.choco.veinminer.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.anticheat.AntiCheatHook;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.economy.SimpleEconomy;
import wtf.choco.veinminer.integration.WorldGuardIntegration;
import wtf.choco.veinminer.manager.VeinMinerManager;
import wtf.choco.veinminer.metrics.StatTracker;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.tool.VeinMinerToolCategoryHand;
import wtf.choco.veinminer.util.AttributeUtil;
import wtf.choco.veinminer.util.VMConstants;
import wtf.choco.veinminer.util.VMEventFactory;

public final class BreakBlockListener implements Listener {

    private static final String METADATA_KEY_BLOCKBREAKEVENT_IGNORE = "blockbreakevent-ignore";

    private final VeinMinerPlugin plugin;

    public BreakBlockListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getClass() != BlockBreakEvent.class) { // For plugins such as McMMO, who fire custom events
            return;
        }

        Block origin = event.getBlock();
        if (origin.hasMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED) || origin.hasMetadata(METADATA_KEY_BLOCKBREAKEVENT_IGNORE)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(item, cat -> player.hasPermission(VMConstants.PERMISSION_VEINMINE.apply(cat)));
        if (category == null) {
            return;
        }

        VeinMinerManager veinMinerManager = plugin.getVeinMinerManager();
        BlockData originBlockData = origin.getBlockData();

        if (!veinMinerManager.isVeinMineable(originBlockData, category)) {
            return;
        }

        // Invalid player state check
        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
        if (veinMinerPlayer == null) {
            return;
        }

        if (!veinMinerPlayer.isVeinMinerActive()
                || !veinMinerPlayer.isVeinMinerEnabled(category)
                || plugin.getConfiguration().isDisabledGameMode(player.getGameMode())
                || category.getConfiguration().isDisabledWorld(origin.getWorld().getName())
                || !player.hasPermission(VMConstants.PERMISSION_VEINMINE.apply(category))) {
            return;
        }

        // WorldGuard check
        boolean worldGuard = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
        if (worldGuard && !WorldGuardIntegration.queryFlagVeinMiner(origin, player)) {
            player.sendMessage(ChatColor.GRAY + "You are not allowed to vein mine in this area.");
            return;
        }

        // Economy check
        double cost = category.getConfiguration().getCost();
        if (cost > 0) {
            SimpleEconomy economy = plugin.getEconomy();
            if (economy.shouldCharge(player)) {
                if (!economy.hasSufficientBalance(player, cost)) {
                    player.sendMessage(ChatColor.GRAY + "You have insufficient funds to vein mine (Required: " + ChatColor.YELLOW + cost + ChatColor.GRAY + ")");
                    return;
                }

                economy.withdraw(player, cost);
            }
        }

        // Fetch the target block face
        double reachDistance = AttributeUtil.getReachDistance(player);
        BlockFace targetBlockFace;
        RayTraceResult rayTraceResult = player.rayTraceBlocks(reachDistance, FluidCollisionMode.NEVER);
        if (rayTraceResult == null || (targetBlockFace = rayTraceResult.getHitBlockFace()) == null) {
            return;
        }

        // TIME TO VEINMINE
        VeinMinerBlock originVeinMinerBlock = veinMinerManager.getVeinMinerBlock(originBlockData, category);
        assert originVeinMinerBlock != null; // If this is null, something is broken internally

        VeinMiningPattern pattern = veinMinerPlayer.getVeinMiningPattern();
        BlockList aliasBlockList = veinMinerManager.getAliases(originVeinMinerBlock);

        List<Block> blocks = new ArrayList<>(pattern.allocateBlocks(origin, targetBlockFace, originVeinMinerBlock, category.getConfiguration(), aliasBlockList));
        blocks.removeIf(Block::isEmpty);

        if (blocks.isEmpty()) {
            return;
        }

        // Fire a new PlayerVeinMineEvent
        PlayerVeinMineEvent veinmineEvent = VMEventFactory.callPlayerVeinMineEvent(player, origin, originVeinMinerBlock, item, category, blocks, pattern);
        if (veinmineEvent.isCancelled() || blocks.isEmpty()) {
            return;
        }

        // Apply metadata to all blocks to be vein mined and all other relevant objects/entities
        veinMinerPlayer.setVeinMining(true);
        blocks.forEach(block -> {
            block.setMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED, new FixedMetadataValue(plugin, true));
            block.setMetadata(VMConstants.METADATA_KEY_VEINMINER_SOURCE, new LazyMetadataValue(plugin, CacheStrategy.CACHE_ETERNALLY, origin::getLocation));
        });

        ExperienceTracker experienceTracker = null;
        if (plugin.getConfiguration().isCollectExperienceAtSource()) {
            experienceTracker = new ExperienceTracker();
            origin.setMetadata(VMConstants.METADATA_KEY_VEINMINER_EXPERIENCE, new FixedMetadataValue(plugin, experienceTracker));
        }

        // Anticheat support
        List<AntiCheatHook> hooks = plugin.getAnticheatHooks();
        hooks.forEach(h -> h.exempt(player));

        // Actually destroying the allocated blocks
        int maxDurability = item.getType().getMaxDurability();
        if (category.getConfiguration().isRepairFriendly()) {
            maxDurability -= 2; // Make sure tools still have enough durability to mine the current block AND other blocks in the vein
        }

        float hungerModifier = plugin.getConfiguration().getHungerModifier() * 0.025F;
        int minimumFoodLevel = plugin.getConfiguration().getMinimumFoodLevel();

        String hungryMessage = plugin.getConfiguration().getHungryMessage();
        if (hungryMessage != null) {
            hungryMessage = ChatColor.translateAlternateColorCodes('&', hungryMessage);
        }

        boolean isHandCategory = category instanceof VeinMinerToolCategoryHand;
        boolean shouldApplyHunger = !player.hasPermission(VMConstants.PERMISSION_FREE_HUNGER);

        for (Block block : blocks) {
            // Apply hunger
            if (hungerModifier != 0.0 && shouldApplyHunger) {
                this.applyHungerDebuff(player, hungerModifier);

                if (player.getFoodLevel() <= minimumFoodLevel) {
                    if (hungryMessage != null) {
                        player.sendMessage(hungryMessage);
                    }

                    break;
                }
            }

            // Check for tool damage
            if (maxDurability > 0 && !isHandCategory) {
                if (item == null || item.getType().isAir()) {
                    break;
                }

                ItemMeta meta = item.getItemMeta();
                if (meta == null || ((Damageable) meta).getDamage() >= maxDurability) {
                    break;
                }
            }

            // Break the block
            Material blockType = block.getType();
            if (block.equals(origin) || player.breakBlock(block)) {
                StatTracker.incrementMinedBlock(blockType);
            }
        }

        // Remove applied metadata
        veinMinerPlayer.setVeinMining(false);
        blocks.forEach(block -> {
            block.removeMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED, plugin);
            block.removeMetadata(VMConstants.METADATA_KEY_VEINMINER_SOURCE, plugin);
        });

        // Handle experience dropping if necessary
        if (experienceTracker != null && experienceTracker.hasExperience()) {
            Location experienceLocation = origin.getLocation().add(0.5, 0.5, 0.5);
            experienceTracker.spawnExperienceOrbsAt(experienceLocation);
            origin.removeMetadata(VMConstants.METADATA_KEY_VEINMINER_EXPERIENCE, plugin);
        }

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
