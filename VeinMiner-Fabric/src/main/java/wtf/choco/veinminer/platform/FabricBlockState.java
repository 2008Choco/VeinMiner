package wtf.choco.veinminer.platform;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.NotNull;

public final class FabricBlockState implements BlockState {

    private String stateString;

    private final Block block;
    private final Map<Property<?>, Comparable<?>> properties;

    public FabricBlockState(Block block, Map<Property<?>, Comparable<?>> properties) {
        this.block = block;
        this.properties = properties;
    }

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
