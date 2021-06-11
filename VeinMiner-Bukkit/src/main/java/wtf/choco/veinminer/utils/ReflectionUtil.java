package wtf.choco.veinminer.utils;

import com.google.common.base.Preconditions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;

public final class ReflectionUtil {

    private static Class<?> nmsPlayer;
    private static Class<?> playerInteractManager;
    private static Field fieldPlayerInteractManager;
    private static Class<?> blockPosition;
    private static Constructor<?> constructorBlockPosition;
    private static Class<?> craftPlayer;
    private static Method methodGetHandle;
    private static Method methodBreakBlock;

    private static boolean isLegacy = true;
    private static String version;

    private ReflectionUtil() { }

    public static boolean breakBlock(@NotNull Player player, @NotNull Block block) {
        Preconditions.checkArgument(player != null, "player must not be null");
        Preconditions.checkArgument(block != null, "block must not be null");

        /*
         * Legacy = 1.13.x - 1.16.x
         */
        if (isLegacy) {
            try {
                Object nmsPlayer = methodGetHandle.invoke(player);
                Object interactManager = fieldPlayerInteractManager.get(nmsPlayer);
                Object blockPosition = constructorBlockPosition.newInstance(block.getX(), block.getY(), block.getZ());

                return (boolean) methodBreakBlock.invoke(interactManager, blockPosition);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }

            return false;
        }

        return player.breakBlock(block);
    }

    public static void init(@NotNull String version) {
        if (ReflectionUtil.version != null) {
            return;
        }

        ReflectionUtil.version = version.concat(".");

        /*
         * We're going to check against this field!
         *
         * The 1.17 CraftBukkit server has these classes under their NMS packages.
         * If it's not where we expect it (net.minecraft.server.EntityPlayer), we're in 1.17
         */
        nmsPlayer = getNMSClass("EntityPlayer");
        if (nmsPlayer == null) {
            isLegacy = false;
            return;
        }

        playerInteractManager = getNMSClass("PlayerInteractManager");
        fieldPlayerInteractManager = getPublicField(nmsPlayer, "playerInteractManager");
        blockPosition = getNMSClass("BlockPosition");
        constructorBlockPosition = getConstructor(blockPosition, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        craftPlayer = getCraftBukkitClass("entity.CraftPlayer");
        methodGetHandle = getMethod("getHandle", craftPlayer);
        methodBreakBlock = getMethod("breakBlock", playerInteractManager, blockPosition);
    }

    private static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            VeinMiner.getPlugin().getLogger().warning("Could not find method " + name + " in " + clazz.getSimpleName());
            isLegacy = false;
        }

        return null;
    }

    private static Field getPublicField(Class<?> clazz, String name) {
        try {
            return clazz.getField(name);
        } catch (Exception e) {
            VeinMiner.getPlugin().getLogger().warning("Failed to reflectively access field. Assuming legacy version.");
            isLegacy = false;
        }

        return null;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException | SecurityException e) {
            VeinMiner.getPlugin().getLogger().warning("Failed to reflectively access constructor. Assuming legacy version.");
            isLegacy = false;
        }

        return null;
    }

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + version + className);
        } catch (Exception e) {
            VeinMiner.getPlugin().getLogger().warning("Failed to reflectively access NMS class. Assuming legacy version.");
            isLegacy = false;
        }

        return null;
    }

    private static Class<?> getCraftBukkitClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + className);
        } catch (Exception e) {
            VeinMiner.getPlugin().getLogger().warning("Failed to reflectively access CraftBukkit class. Assuming legacy version.");
            isLegacy = false;
        }

        return null;
    }

}
