package wtf.choco.veinminer.pattern;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.block.BlockAccessor;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A dummy {@link VeinMiningPattern} that performs no block allocations.
 * <p>
 * This implementation serves as a way to register patterns to a registry without actually
 * providing an implementation of a pattern. Intended primarily for internal use on the
 * client to support server-supplied patterns that the client would otherwise not understand.
 */
@Internal
public final class VeinMiningPatternDummy implements VeinMiningPattern {

    private final NamespacedKey key;

    /**
     * Construct a new {@link VeinMiningPatternDummy}.
     *
     * @param key the pattern key
     */
    public VeinMiningPatternDummy(@NotNull NamespacedKey key) {
        this.key = key;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public Set<BlockPosition> allocateBlocks(@NotNull BlockAccessor blockAccessor, @NotNull BlockPosition origin, @NotNull VeinMinerBlock block, @NotNull VeinMinerConfig config, @Nullable BlockList aliasList) {
        return Collections.emptySet();
    }

}
