package wtf.choco.veinminer.manager;

import com.sk89q.worldedit.world.block.BlockType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * A manager for VeinMiner's general configurable values.
 */
public final class VeinMinerManager {

    private BlockList globalBlockList = new BlockList();
    private List<BlockList> aliases = new ArrayList<>(); // There has to be a better way to implement aliases... I just can't think of one

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link VeinMinerManager}.
     *
     * @param plugin the vein miner instance
     */
    public VeinMinerManager(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Set the global {@link BlockList}.
     * <p>
     * Values in this BlockList should be breakable by all categories.
     *
     * @param blockList the block list to set
     */
    public void setGlobalBlockList(@NotNull BlockList blockList) {
        this.globalBlockList = blockList;
    }

    /**
     * Get the global {@link BlockList}.
     * <p>
     * Values in this BlockList should be breakable by all categories.
     *
     * @return the global block list
     */
    @NotNull
    public BlockList getGlobalBlockList() {
        return globalBlockList;
    }

    /**
     * Get a {@link BlockList} of all {@link VeinMinerBlock VeinMinerBlocks} vein mineable
     * in any category.
     *
     * @return a BlockList containing the blocks of all categories and the global BlockList
     */
    @NotNull
    public BlockList getAllVeinMineableBlocks() {
        BlockList blockList = globalBlockList.clone();

        this.plugin.getToolCategoryRegistry().getAll().forEach(category -> {
            blockList.addAll(category.getBlockList());
        });

        return blockList;
    }

    /**
     * Check whether or not the given {@link BlockData} can be destroyed using vein miner
     * under the given {@link VeinMinerToolCategory}.
     * <p>
     * Convenience method to check both the category's {@link BlockList}, as well as
     * {@link #getGlobalBlockList() the global BlockList}.
     *
     * @param state the state to check
     * @param category the category to check
     *
     * @return true if the state is vein mineable, false otherwise
     */
    public boolean isVeinMineable(@NotNull BlockData state, @NotNull VeinMinerToolCategory category) {
        return globalBlockList.containsState(state) || category.getBlockList().containsState(state);
    }

    /**
     * Check whether or not the given {@link BlockType} can be destroyed using vein miner
     * under the given {@link VeinMinerToolCategory}.
     * <p>
     * Convenience method to check both the category's {@link BlockList}, as well as
     * {@link #getGlobalBlockList() the global BlockList}.
     *
     * @param type the type to check
     * @param category the category to check
     *
     * @return true if the type is vein mineable, false otherwise
     */
    public boolean isVeinMineable(@NotNull Material type, @NotNull VeinMinerToolCategory category) {
        return globalBlockList.containsType(type) || category.getBlockList().containsType(type);
    }

    /**
     * Check whether or not the given {@link BlockData} can be destroyed using vein miner
     * with any category.
     * <p>
     * Convenience method to check all {@link BlockList BlockLists}, including
     * {@link #getGlobalBlockList() the global BlockList}.
     *
     * @param state the state to check
     *
     * @return true if the state is vein mineable, false otherwise
     */
    public boolean isVeinMineable(@NotNull BlockData state) {
        if (globalBlockList.containsState(state)) {
            return true;
        }

        for (VeinMinerToolCategory category : plugin.getToolCategoryRegistry().getAll()) {
            if (category.getBlockList().containsState(state)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether or not the given {@link Material} can be destroyed using vein miner
     * with any category.
     * <p>
     * Convenience method to check all {@link BlockList BlockLists}, including
     * {@link #getGlobalBlockList() the global BlockList}.
     *
     * @param type the type to check
     *
     * @return true if the type is vein mineable, false otherwise
     */
    public boolean isVeinMineable(@NotNull Material type) {
        if (globalBlockList.containsType(type)) {
            return true;
        }

        for (VeinMinerToolCategory category : plugin.getToolCategoryRegistry().getAll()) {
            if (category.getBlockList().containsType(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the {@link VeinMinerBlock} instance from the provided {@link VeinMinerToolCategory}'s
     * {@link BlockList} that matches the given {@link BlockData}.
     *
     * @param state the state whose matching VeinMinerBlock instance to get
     * @param category the category whose BlockList from which the instance should be fetched
     *
     * @return the matching VeinMinerBlock, or null if none exists
     */
    @Nullable
    public VeinMinerBlock getVeinMinerBlock(@NotNull BlockData state, @NotNull VeinMinerToolCategory category) {
        VeinMinerBlock block = globalBlockList.getVeinMinerBlock(state);
        return (block != null) ? block : category.getBlockList().getVeinMinerBlock(state);
    }

    /**
     * Add a new alias {@link BlockList}.
     *
     * @param blockList the list of blocks in this alias
     *
     * @return true if the alias was added, false if it was already added
     */
    public boolean addAlias(@NotNull BlockList blockList) {
        if (aliases.contains(blockList)) {
            return false;
        }

        this.aliases.add(blockList);
        return true;
    }

    /**
     * Remove an alias {@link BlockList}.
     *
     * @param blockList the alias block list to remove
     *
     * @return true if the alias was removed, false if it was not already added
     */
    public boolean removeAlias(@NotNull BlockList blockList) {
        return aliases.remove(blockList);
    }

    /**
     * Remove an alias that contains the given {@link VeinMinerBlock}.
     *
     * @param block the block
     *
     * @return the removed alias block list, or null if none contained the block
     */
    @Nullable
    public BlockList removeAliasContaining(@NotNull VeinMinerBlock block) {
        Iterator<BlockList> aliasIterator = aliases.iterator();

        while (aliasIterator.hasNext()) {
            BlockList blockList = aliasIterator.next();

            if (blockList.contains(block)) {
                aliasIterator.remove();
                return blockList;
            }
        }

        return null;
    }

    /**
     * Get an alias {@link BlockList} that contains the given {@link VeinMinerBlock}.
     *
     * @param block the block
     *
     * @return the alias block list to which the block belongs
     */
    @Nullable
    public BlockList getAlias(@NotNull VeinMinerBlock block) {
        for (BlockList blockList : aliases) {
            if (blockList.contains(block)) {
                return blockList;
            }
        }

        return null;
    }

    /**
     * Clear current values stored inside this manager and reload them with values set
     * in VeinMiner's configuration.
     */
    public void reloadFromConfig() {
        this.clear();

        VeinMinerConfiguration config = plugin.getConfiguration();

        // Global block list
        this.setGlobalBlockList(BlockList.parseBlockList(config.getGlobalBlockListKeys(), plugin.getLogger()));

        // Aliases
        int aliasesAdded = 0;
        for (String aliasString : config.getAliasStrings()) {
            List<String> aliasStringEntries = List.of(aliasString.split(";"));
            if (aliasStringEntries.size() <= 1 && !aliasStringEntries.get(0).startsWith("#")) {
                this.plugin.getLogger().warning("Alias \"%s\" contains %d entries but must have at least 2, or be a tag. Ignoring...".formatted(aliasString, aliasStringEntries.size()));
                continue;
            }

            BlockList aliasBlockList = BlockList.parseBlockList(aliasStringEntries, plugin.getLogger());
            if (aliasBlockList.isEmpty()) {
                continue;
            }

            this.addAlias(aliasBlockList);
            aliasesAdded++;
        }

        this.plugin.getLogger().info("Added " + aliasesAdded + " aliases.");
    }

    /**
     * Clear values stored in this {@link VeinMinerManager}.
     */
    public void clear() {
        this.globalBlockList.clear();
        this.aliases.clear();
    }

}
