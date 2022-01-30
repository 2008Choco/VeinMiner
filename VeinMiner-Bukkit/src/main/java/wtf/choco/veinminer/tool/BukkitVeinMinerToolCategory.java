package wtf.choco.veinminer.tool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.platform.BukkitItemType;
import wtf.choco.veinminer.platform.ItemType;

/**
 * A Bukkit implementation of {@link VeinMinerToolCategory}.
 */
public class BukkitVeinMinerToolCategory implements VeinMinerToolCategory {

    private final String id;
    private final int priority;
    private final BlockList blockList;
    private final VeinMinerConfig config;
    private final Set<ItemType> items;

    /**
     * Construct a new {@link BukkitVeinMinerToolCategory}.
     *
     * @param id the unique id of the tool category
     * @param priority the category's priority
     * @param blockList the category block list
     * @param config the category config
     * @param items the items in this category
     */
    public BukkitVeinMinerToolCategory(@NotNull String id, int priority, @NotNull BlockList blockList, @NotNull VeinMinerConfig config, @NotNull Set<Material> items) {
        this.id = id;
        this.priority = priority;
        this.blockList = blockList;
        this.config = config;

        this.items = new HashSet<>();
        items.forEach(material -> this.items.add(BukkitItemType.of(material)));
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @NotNull
    @Override
    public BlockList getBlockList() {
        return blockList;
    }

    @NotNull
    @Override
    public VeinMinerConfig getConfig() {
        return config;
    }

    @Override
    public boolean addItem(@NotNull ItemType itemType) {
        return items.add(itemType);
    }

    @Override
    public boolean removeItem(@NotNull ItemType itemType) {
        return items.remove(itemType);
    }

    @NotNull
    @UnmodifiableView
    @Override
    public Set<ItemType> getItems() {
        return Collections.unmodifiableSet(items);
    }

    @Override
    public boolean containsItem(@NotNull ItemType item) {
        return items.contains(item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockList, config, id, items);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BukkitVeinMinerToolCategory other)) {
            return false;
        }

        return Objects.equals(id, other.id)
                && Objects.equals(blockList, other.blockList)
                && Objects.equals(config, other.config)
                && Objects.equals(items, other.items);
    }

    @Override
    public String toString() {
        StringJoiner itemJoiner = new StringJoiner(", ", "\"", "\"");
        this.items.forEach(item -> itemJoiner.add(item.getKey().toString()));

        return String.format("BukkitVeinMinerToolCategory[id=\"%s\", blockList=\"%s\", config=\"%s\", items=%s]", id, blockList, config, itemJoiner.toString());
    }

}
