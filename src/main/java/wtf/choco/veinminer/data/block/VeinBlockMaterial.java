package wtf.choco.veinminer.data.block;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

class VeinBlockMaterial implements VeinBlock {

    private final Material type;
    private final BlockData data;

    protected VeinBlockMaterial(@NotNull Material type) {
        Preconditions.checkArgument(type != null, "Cannot create material block for null type");
        this.type = type;
        this.data = type.createBlockData("[]");
    }

    @Override
    public Material getType() {
        return type;
    }

    @Override
    public boolean hasSpecificData() {
        return false;
    }

    @Override
    public BlockData getBlockData() {
        return data.clone();
    }

    @Override
    public boolean encapsulates(Block block) {
        return block != null && block.getType() == type;
    }

    @Override
    public boolean encapsulates(BlockData data) {
        return data != null && data.getMaterial() == type;
    }

    @Override
    public boolean encapsulates(Material material) {
        return material == type;
    }

    @Override
    public String asDataString() {
        return type.getKey().toString();
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || ((obj instanceof VeinBlockMaterial) && ((VeinBlockMaterial) obj).type == type);
    }

    @Override
    public String toString() {
        return "{VeinBlockMaterial:{\"Type\":\"" + asDataString() + "\"}}";
    }

}
