package wtf.choco.veinminer.listener;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.metadata.MetadataValue;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.utils.VMConstants;

public final class ItemCollectionListener implements Listener {

    private final VeinMiner plugin;

    public ItemCollectionListener(VeinMiner plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onDropVeinMinedItem(BlockDropItemEvent event) {
        Block block = event.getBlock();
        if (!block.hasMetadata(VMConstants.METADATA_KEY_TO_BE_VEINMINED)) {
            return;
        }

        if (!plugin.getConfig().getBoolean(VMConstants.CONFIG_COLLECT_ITEMS_AT_SOURCE, true)) {
            return;
        }

        Location source = null;
        List<MetadataValue> sourceMetadataValues = block.getMetadata(VMConstants.METADATA_KEY_VEINMINER_SOURCE);
        for (MetadataValue sourceMetadataValue : sourceMetadataValues) {
            Object sourceObject = sourceMetadataValue.value();
            if (sourceObject instanceof Location) {
                source = (Location) sourceObject;
            }
        }

        if (source == null) {
            return;
        }

        Location sourceFinal = source.clone().add(0.5, 0.5, 0.5);
        event.getItems().forEach(item -> item.teleport(sourceFinal));
    }

}
