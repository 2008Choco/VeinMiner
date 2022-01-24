package wtf.choco.veinminer.tool;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.platform.ItemType;

/**
 * A Bukkit implementation of {@link VeinMinerToolCategory}.
 */
public final class BukkitVeinMinerToolCategoryHand extends BukkitVeinMinerToolCategory {

    /**
     * Construct a new {@link BukkitVeinMinerToolCategoryHand}.
     *
     * @param blockList the category block list
     * @param config the category config
     */
    public BukkitVeinMinerToolCategoryHand(@NotNull BlockList blockList, @NotNull VeinMinerConfig config) {
        super("Hand", blockList, config, Collections.emptySet());
    }

    @Override
    public boolean addItem(@NotNull ItemType itemType) {
        throw new UnsupportedOperationException("Cannot add ItemType to Hand category.");
    }

    @Override
    public boolean removeItem(@NotNull ItemType itemType) {
        throw new UnsupportedOperationException("Cannot remove ItemType from Hand category.");
    }

    @Override
    public boolean containsItem(@NotNull ItemType item) {
        return item.getKey().toString().equals("minecraft:air"); // Hand category activates only if the type is air
    }

}
