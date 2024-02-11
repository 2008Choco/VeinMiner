package wtf.choco.veinminer.config;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.data.PersistentDataStorage;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * Represents a configuration contract for VeinMiner.
 * <p>
 * Note that any methods declared in this interface are pulled from a configuration file and
 * should not be confused with any similarly named methods that represent a stored state.
 */
public interface VeinMinerConfiguration extends VeinMiningConfiguration {

    public boolean isMetricsEnabled();

    public boolean isPerformUpdateChecks();

    @NotNull
    public ActivationStrategy getDefaultActivationStrategy();

    @NotNull
    public VeinMiningPattern getDefaultVeinMiningPattern();

    public boolean isCollectItemsAtSource();

    public boolean isNerfMcMMO();

    public boolean isDisabledGameMode(@NotNull GameMode gameMode);

    @NotNull
    @Unmodifiable
    public Set<GameMode> getDisabledGameModes();

    public float getHungerModifier();

    public int getMinimumFoodLevel();

    @Nullable
    public String getHungryMessage();

    @NotNull
    public ClientConfig getClientConfiguration();

    public default boolean isAllowActivationKeybind() {
        return getClientConfiguration().isAllowActivationKeybind();
    }

    public default boolean isAllowPatternSwitchingKeybind() {
        return getClientConfiguration().isAllowPatternSwitchingKeybind();
    }

    public default boolean isAllowWireframeRendering() {
        return getClientConfiguration().isAllowWireframeRendering();
    }

    @NotNull
    public PersistentDataStorage.Type getStorageType();

    @Nullable
    public File getJsonStorageDirectory();

    @Nullable
    public String getMySQLHost();

    public int getMySQLPort();

    @Nullable
    public String getMySQLUsername();

    @Nullable
    public String getMySQLPassword();

    @Nullable
    public String getMySQLDatabase();

    @Nullable
    public String getMySQLTablePrefix();

    @NotNull
    @Unmodifiable
    public Collection<String> getGlobalBlockListKeys();

    public void setBlockListKeys(@NotNull String categoryId, @NotNull BlockList blockList);

    public default void setBlockListKeys(@NotNull VeinMinerToolCategory category, @NotNull BlockList blockList) {
        this.setBlockListKeys(category.getId(), blockList);
    }

    @NotNull
    @Unmodifiable
    public Collection<String> getBlockListKeys(@NotNull String categoryId);

    @NotNull
    @Unmodifiable
    public Collection<String> getAliasStrings();

    @NotNull
    @Unmodifiable
    public Collection<String> getDefinedCategoryIds();

    @Nullable
    public ToolCategoryConfiguration getToolCategoryConfiguration(@NotNull String categoryId);

}
