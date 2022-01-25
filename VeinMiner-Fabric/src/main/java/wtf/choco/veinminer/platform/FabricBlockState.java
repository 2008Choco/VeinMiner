package wtf.choco.veinminer.platform;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.NotNull;

/**
 * A Fabric implementation of {@link BlockState}.
 */
public final class FabricBlockState implements BlockState {

    private String stateString;

    private final Block block;
    private final Map<Property<?>, Comparable<?>> properties;

    /**
     * Construct a new {@link FabricBlockState} for the given {@link Block} and properties.
     * <p>
     * Only the properties in the provided Map will be considered explicitly set. Any
     * properties of the given Block not present in the map will be ignored when checking
     * if it {@link #matches(BlockState)} another {@link BlockState}.
     *
     * @param block the type of block for which to create a BlockState
     * @param properties the explicitly set properties
     */
    public FabricBlockState(Block block, Map<Property<?>, Comparable<?>> properties) {
        this.block = block;
        this.properties = properties;
    }

    /**
     * Construct a new {@link FabricBlockState} for the given
     * {@link net.minecraft.block.BlockState BlockState}.
     * <p>
     * Unlike {@link FabricBlockState#FabricBlockState(Block, Map)}, this constructor
     * considers all states to be explicitly set. Therefore, this constructor should be
     * used only as an argument to {@link #matches(BlockState)}, not as an invoker.
     *
     * @param state the state to wrap
     */
    public FabricBlockState(net.minecraft.block.BlockState state) {
        this.block = state.getBlock();
        this.properties = state.getEntries();
    }

    @NotNull
    @Override
    public BlockType getType() {
        return FabricBlockType.of(block);
    }

    @NotNull
    @Override
    public String getAsString(boolean hideUnspecified) {
        if (stateString == null) {
            StringBuilder string = new StringBuilder(Registry.BLOCK.getId(block).toString());

            if (!properties.isEmpty()) {
                string.append('[');
                string.append(properties.entrySet().stream().map(entry -> entry.getKey().getName() + "=" + entry.getValue()).collect(Collectors.joining(",")));
                string.append(']');
            }

            this.stateString = string.toString();
        }

        return stateString;
    }

    @Override
    public boolean matches(@NotNull BlockState state) {
        if (!(state instanceof FabricBlockState fabricState) || !Objects.equals(block, fabricState.block)) {
            return false;
        }

        for (Map.Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
            Property<?> property = entry.getKey();
            Comparable<?> value = entry.getValue();

            if (!Objects.equals(value, fabricState.properties.get(property))) {
                return false;
            }
        }

        return true;
    }

}
