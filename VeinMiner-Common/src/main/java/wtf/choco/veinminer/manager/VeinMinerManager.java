package wtf.choco.veinminer.manager;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.platform.BlockState;
import wtf.choco.veinminer.platform.BlockType;
import wtf.choco.veinminer.platform.GameMode;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

public final class VeinMinerManager {

    private BlockList globalBlockList = new BlockList();
    private VeinMinerConfig globalConfig = new VeinMinerConfig();

    private final Set<GameMode> disabledGameModes = EnumSet.noneOf(GameMode.class);

    public void setGlobalBlockList(@NotNull BlockList globalBlockList) {
        this.globalBlockList = globalBlockList;
    }

    @NotNull
    public BlockList getGlobalBlockList() {
        return globalBlockList;
    }

    public void setGlobalConfig(@NotNull VeinMinerConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @NotNull
    public VeinMinerConfig getGlobalConfig() {
        return globalConfig;
    }

    public boolean isDisabledGameMode(@NotNull GameMode gameMode) {
        return disabledGameModes.contains(gameMode);
    }

    public void setDisabledGameModes(@NotNull Set<GameMode> disabledGameModes) {
        this.disabledGameModes.clear();
        this.disabledGameModes.addAll(disabledGameModes);
    }

    @NotNull
    @UnmodifiableView
    public Set<GameMode> getDisabledGameModes() {
        return Collections.unmodifiableSet(disabledGameModes);
    }

    @NotNull
    public BlockList getAllVeinMineableBlocks() {
        BlockList blockList = globalBlockList.clone();

        VeinMiner.getInstance().getToolCategoryRegistry().getAll().forEach(category -> {
            blockList.addAll(category.getBlockList());
        });

        return blockList;
    }

    public boolean isVeinMineable(@NotNull BlockState state, @NotNull VeinMinerToolCategory category) {
        return globalBlockList.containsState(state) || category.getBlockList().containsState(state);
    }

    public boolean isVeinMineable(@NotNull BlockType type, @NotNull VeinMinerToolCategory category) {
        return globalBlockList.containsType(type) || category.getBlockList().containsType(type);
    }

    public boolean isVeinMineable(@NotNull BlockState state) {
        if (globalBlockList.containsState(state)) {
            return true;
        }

        for (VeinMinerToolCategory category : VeinMiner.getInstance().getToolCategoryRegistry().getAll()) {
            if (category.getBlockList().containsState(state)) {
                return true;
            }
        }

        return false;
    }

    public boolean isVeinMineable(@NotNull BlockType type) {
        if (globalBlockList.containsType(type)) {
            return true;
        }

        for (VeinMinerToolCategory category : VeinMiner.getInstance().getToolCategoryRegistry().getAll()) {
            if (category.getBlockList().containsType(type)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public VeinMinerBlock getVeinMinerBlock(@NotNull BlockState state, @NotNull VeinMinerToolCategory category) {
        VeinMinerBlock block = globalBlockList.getVeinMinerBlock(state);
        return (block != null) ? block : category.getBlockList().getVeinMinerBlock(state);
    }

    public void clear() {
        this.globalBlockList.clear();
        this.disabledGameModes.clear();
    }

}
