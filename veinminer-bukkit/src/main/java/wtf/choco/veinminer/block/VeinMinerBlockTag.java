package wtf.choco.veinminer.block;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A type of {@link VeinMinerBlock} backed by a block {@link Tag}.
 */
public final class VeinMinerBlockTag implements VeinMinerBlock {

    private final Tag<Material> tag;

    /**
     * Construct a new {@link VeinMinerBlockTag}.
     *
     * @param tag the block tag
     */
    public VeinMinerBlockTag(Tag<Material> tag) {
        this.tag = tag;
    }

    public Tag<Material> getTag() {
        return tag;
    }

    @Override
    public boolean isTangible() {
        return false;
    }

    @Override
    public boolean matchesType(@NotNull Material type) {
        return tag.isTagged(type);
    }

    @Override
    public boolean matchesState(@NotNull BlockData state, boolean exact) {
        return matchesType(state.getMaterial());
    }

    @NotNull
    @Override
    public String toStateString() {
        return "#" + tag.getKey().toString();
    }

    @Override
    public int hashCode() {
        return tag.getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof VeinMinerBlockTag other && tag.getKey().equals(other.tag.getKey()));
    }

    @Override
    public String toString() {
        return String.format("VeinMinerBlockTag[type=\"%s\"]", tag.getKey().toString());
    }

}
