package wtf.choco.veinminer.config;

import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.block.BlockList;

public interface ToolCategoryConfiguration extends VeinMiningConfiguration {

    public int getPriority();

    @Nullable
    public String getNBTValue();

    public void setItems(@NotNull List<Material> items);

    @NotNull
    @Unmodifiable
    public Collection<String> getItemKeys();

    public void setBlockListKeys(@NotNull BlockList blockList);

    @NotNull
    @Unmodifiable
    public Collection<String> getBlockListKeys();

}
