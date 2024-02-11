package wtf.choco.veinminer.tool;

import java.util.Collections;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;

/**
 * A more specific type of {@link VeinMinerToolCategory} that represents the hand category.
 * No items may be added to or removed from this category.
 */
public final class VeinMinerToolCategoryHand extends VeinMinerToolCategory {

    /**
     * Construct a new {@link VeinMinerToolCategoryHand}.
     *
     * @param blockList the category block list
     * @param config the category config
     */
    public VeinMinerToolCategoryHand(@NotNull BlockList blockList, @NotNull ToolCategoryConfiguration config) {
        super("Hand", Integer.MAX_VALUE, null, blockList, config, Collections.emptySet());
    }

    @Override
    public boolean addItem(@NotNull Material itemType) {
        throw new UnsupportedOperationException("Cannot add ItemType to Hand category.");
    }

    @Override
    public boolean removeItem(@NotNull Material itemType) {
        throw new UnsupportedOperationException("Cannot remove ItemType from Hand category.");
    }

    @Override
    public boolean containsItem(@NotNull Material item) {
        return item.isAir(); // Hand category activates only if the type is air
    }

}
