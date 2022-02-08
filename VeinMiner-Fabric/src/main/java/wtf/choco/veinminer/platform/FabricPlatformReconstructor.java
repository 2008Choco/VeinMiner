package wtf.choco.veinminer.platform;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Fabric implementation of {@link PlatformReconstructor}.
 */
public final class FabricPlatformReconstructor implements PlatformReconstructor {

    /**
     * The singleton instance of the {@link FabricPlatformReconstructor}.
     */
    public static final PlatformReconstructor INSTANCE = new FabricPlatformReconstructor();

    private FabricPlatformReconstructor() { }

    @Nullable
    @Override
    public BlockState getState(@NotNull String state) {
        try {
            BlockArgumentParser parser = new BlockArgumentParser(new StringReader(state), false).parse(false);

            net.minecraft.block.BlockState minecraftState = parser.getBlockState();
            if (minecraftState == null) {
                return null;
            }

            return new FabricBlockState(minecraftState.getBlock(), parser.getBlockProperties());
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockType getBlockType(@NotNull String type) {
        Identifier identifier = Identifier.tryParse(type);
        if (identifier == null) {
            return null;
        }

        Optional<Block> block = Registry.BLOCK.getOrEmpty(identifier);
        return block.isPresent() ? block.map(FabricBlockType::of).get() : null;
    }

    @Nullable
    @Override
    public ItemType getItemType(@NotNull String type) {
        Identifier identifier = Identifier.tryParse(type);
        if (identifier == null) {
            return null;
        }

        Optional<Item> item = Registry.ITEM.getOrEmpty(identifier);
        return item.isPresent() ? item.map(FabricItemType::of).get() : null;
    }

}
