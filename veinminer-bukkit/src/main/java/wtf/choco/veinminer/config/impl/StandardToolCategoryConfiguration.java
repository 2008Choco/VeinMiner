package wtf.choco.veinminer.config.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.ConfigWrapper;
import wtf.choco.veinminer.config.ToolCategoryConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;

import static wtf.choco.veinminer.config.impl.ConfigKeys.*;

/**
 * A standard {@link ToolCategoryConfiguration} implementation.
 */
public final class StandardToolCategoryConfiguration implements ToolCategoryConfiguration {

    private final String categoryId;
    private final ConfigWrapper categoriesConfig;
    private final VeinMinerConfiguration parent;

    /**
     * Construct a new {@link StandardToolCategoryConfiguration}.
     *
     * @param categoryId the id of the category for this configuration
     * @param categoriesConfig the {@link ConfigWrapper} instance for the categories.yml
     * @param parent the parent {@link VeinMinerConfiguration}
     */
    StandardToolCategoryConfiguration(@NotNull String categoryId, @NotNull ConfigWrapper categoriesConfig, @NotNull VeinMinerConfiguration parent) {
        this.categoryId = categoryId;
        this.categoriesConfig = categoriesConfig;
        this.parent = parent;
    }

    @Override
    public boolean isRepairFriendly() {
        return getCategoryConfig().getBoolean(KEY_REPAIR_FRIENDLY, parent.isRepairFriendly());
    }

    @Override
    public int getMaxVeinSize() {
        return getCategoryConfig().getInt(KEY_MAX_VEIN_SIZE, parent.getMaxVeinSize());
    }

    @Override
    public double getCost() {
        return getCategoryConfig().getDouble(KEY_COST, parent.getCost());
    }

    @Override
    public boolean isDisabledWorld(@NotNull String worldName) {
        return getDisabledWorlds().contains(worldName);
    }

    @NotNull
    @Unmodifiable
    @Override
    public Set<String> getDisabledWorlds() {
        if (!getCategoryConfig().contains(KEY_DISABLED_WORLDS, true)) {
            return parent.getDisabledWorlds();
        }

        return ImmutableSet.copyOf(getCategoryConfig().getStringList(KEY_DISABLED_WORLDS));
    }

    @Override
    public int getPriority() {
        return getCategoryConfig().getInt(KEY_PRIORITY, 0);
    }

    @Nullable
    @Override
    public String getNBTValue() {
        return getCategoryConfig().getString(KEY_NBT);
    }

    @Override
    public void setItems(@NotNull List<Material> items) {
        List<String> itemListStrings = items.stream()
                .map(item -> item.getKey().toString())
                .sorted().toList();

        this.getCategoryConfig().set(KEY_ITEMS, itemListStrings);
        this.categoriesConfig.save();
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getItemKeys() {
        return ImmutableList.copyOf(getCategoryConfig().getStringList(KEY_ITEMS));
    }


    @Override
    public void setBlockListKeys(@NotNull BlockList blockList) {
        List<String> blockListStrings = blockList.asList().stream()
                .map(VeinMinerBlock::toStateString)
                .sorted().toList();

        this.getCategoryConfig().set(KEY_BLOCK_LIST, blockListStrings);
        this.categoriesConfig.save();
    }

    @NotNull
    @Unmodifiable
    @Override
    public Collection<String> getBlockListKeys() {
        return ImmutableList.copyOf(getCategoryConfig().getStringList(KEY_BLOCK_LIST));
    }

    @NotNull
    private ConfigurationSection getCategoryConfig() {
        ConfigurationSection section = categoriesConfig.asRawConfig().getConfigurationSection(categoryId);

        if (section == null) {
            throw new IllegalStateException("Configuration for category \"" + categoryId + "\" does not exist.");
        }

        return section;
    }

}
