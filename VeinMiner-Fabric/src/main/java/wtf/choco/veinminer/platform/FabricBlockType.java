package wtf.choco.veinminer.platform;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A Fabric implementation of {@link BlockType}.
 */
public final class FabricBlockType implements BlockType {

    private static final Map<Block, BlockType> CACHE = new HashMap<>();

    private final NamespacedKey key;

    private FabricBlockType(Block block) {
        Identifier id = Registry.BLOCK.getId(block);
        this.key = new NamespacedKey(id.getNamespace(), id.getPath());
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public BlockState createBlockState(@NotNull String states) {
        try {
            BlockArgumentParser parser = new BlockArgumentParser(new StringReader(key.toString() + "[" + states + "]"), false).parse(false);

            net.minecraft.block.BlockState state = parser.getBlockState();
            if (state == null) {
                throw new IllegalArgumentException(String.format("Unsupported state string, \"%s\"", states));
            }

            return new FabricBlockState(state.getBlock(), parser.getBlockProperties());
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(String.format("Unsupported state string, \"%s\"", states), e);
        }
    }

    /**
     * Get a {@link BlockType} for the given {@link Block}.
     *
     * @param block the block
     *
     * @return the BlockType instance
     */
    public static BlockType of(Block block) {
        return CACHE.computeIfAbsent(block, FabricBlockType::new);
    }

}
