package wtf.choco.veinminer.listener;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.util.VMConstants;

public final class BlockDropCollectionListener implements Listener {

    private final VeinMinerPlugin plugin;

    public BlockDropCollectionListener(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onDropVeinMinedItem(BlockDropItemEvent event) {
        Block block = event.getBlock();
        if (!block.hasMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED)) {
            return;
        }

        if (!plugin.getConfiguration().isCollectItemsAtSource()) {
            return;
        }

        Location source = find(block, VMConstants.METADATA_KEY_VEINMINER_SOURCE, Location.class);
        if (source == null) {
            return;
        }

        Location sourceFinal = source.clone().add(0.5, 0.5, 0.5);
        event.getItems().forEach(item -> item.teleport(sourceFinal));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onDropVeinMinedExperience(BlockExpEvent event) {
        int experience = event.getExpToDrop();
        if (experience <= 0) {
            return;
        }

        Block block = event.getBlock();
        if (!block.hasMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED)) {
            return;
        }

        if (!plugin.getConfiguration().isCollectExperienceAtSource()) {
            return;
        }

        Location source = find(block, VMConstants.METADATA_KEY_VEINMINER_SOURCE, Location.class);
        if (source == null) {
            return;
        }

        Block sourceBlock = source.getBlock();
        ExperienceTracker experienceTracker = find(sourceBlock, VMConstants.METADATA_KEY_VEINMINER_EXPERIENCE, ExperienceTracker.class);
        if (experienceTracker == null) {
            return;
        }

        experienceTracker.pushExperience(experience);
        event.setExpToDrop(0);
    }

    @Nullable
    private <T> T find(@NotNull Block block, @NotNull String metadataKey, @NotNull Class<T> clazz) {
        List<MetadataValue> values = block.getMetadata(metadataKey);

        for (MetadataValue value : values) {
            Object sourceObject = value.value();
            if (clazz.isInstance(sourceObject)) {
                return clazz.cast(sourceObject);
            }
        }

        return null;
    }

}
