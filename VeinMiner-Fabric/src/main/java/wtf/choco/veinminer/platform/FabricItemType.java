package wtf.choco.veinminer.platform;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A Fabric implementation of {@link ItemType}.
 */
public final class FabricItemType implements ItemType {

    private static final Map<Item, ItemType> CACHE = new HashMap<>();

    private final NamespacedKey key;

    private FabricItemType(Item item) {
        Identifier id = Registry.ITEM.getId(item);
        this.key = new NamespacedKey(id.getNamespace(), id.getPath());
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Get an {@link ItemType} for the given {@link Item}.
     *
     * @param item the item
     *
     * @return the ItemType instance
     */
    public static ItemType of(Item item) {
        return CACHE.computeIfAbsent(item, FabricItemType::new);
    }

}
