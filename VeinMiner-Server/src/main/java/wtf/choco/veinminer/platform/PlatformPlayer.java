package wtf.choco.veinminer.platform;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.world.BlockAccessor;
import wtf.choco.veinminer.platform.world.ItemStack;
import wtf.choco.veinminer.platform.world.RayTraceResult;
import wtf.choco.veinminer.util.NamespacedKey;

public interface PlatformPlayer {

    public boolean isOnline();

    @NotNull
    public String getName();

    @NotNull
    public UUID getUniqueId();

    @NotNull
    public BlockAccessor getWorld();

    @NotNull
    public ItemStack getItemInMainHand();

    @NotNull
    public RayTraceResult getTargetBlock(int distance);

    public void sendMessage(@NotNull String message);

    public boolean hasPermission(@NotNull String permission);

    @NotNull
    public GameMode getGameMode();

    public boolean isSneaking();

    public void sendPluginMessage(@NotNull NamespacedKey channel, byte[] message);

    public void kick(@NotNull String message);

}
