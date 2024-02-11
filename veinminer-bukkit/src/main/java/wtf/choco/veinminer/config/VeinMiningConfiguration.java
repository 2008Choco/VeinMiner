package wtf.choco.veinminer.config;

import java.util.Set;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface VeinMiningConfiguration {

    public boolean isRepairFriendly();

    public int getMaxVeinSize();

    public double getCost();

    public boolean isDisabledWorld(@NotNull String worldName);

    public default boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    @NotNull
    @Unmodifiable
    public Set<String> getDisabledWorlds();

}
