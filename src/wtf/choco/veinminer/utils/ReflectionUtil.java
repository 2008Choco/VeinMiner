package wtf.choco.veinminer.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Preconditions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ReflectionUtil {
	
	private static Class<?> nmsPlayer;
	private static Class<?> playerInteractManager;
	private static Field fieldPlayerInteractManager;
	private static Class<?> blockPosition;
	private static Constructor<?> constructorBlockPosition;
	private static Class<?> craftPlayer;
	private static Method methodGetHandle;
	private static Method methodBreakBlock;
	
	private static boolean wasSuccessful = false;
	private static String version;
	
	private ReflectionUtil() { }
	
	public static void breakBlock(Player player, Block block) {
		Preconditions.checkNotNull(player, "A null player is incapable of breaking blocks");
		Preconditions.checkNotNull(block, "Cannot break a null block");
		
		if (!wasSuccessful) {
			block.breakNaturally(player.getInventory().getItemInMainHand());
			return;
		}
		
		try {
			Object cPlayer = methodGetHandle.invoke(player);
			Object interactManager = fieldPlayerInteractManager.get(cPlayer);
			Object blockPos = constructorBlockPosition.newInstance(block.getX(), block.getY(), block.getZ());
			methodBreakBlock.invoke(interactManager, blockPos);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadNMSClasses(String version) {
		if (nmsPlayer != null) return;
		
		ReflectionUtil.version = version.concat(".");
		
		nmsPlayer = getNMSClass("EntityPlayer");
		playerInteractManager = getNMSClass("PlayerInteractManager");
		fieldPlayerInteractManager = getField(nmsPlayer, "playerInteractManager");
		blockPosition = getNMSClass("BlockPosition");
		constructorBlockPosition = getConstructor(blockPosition, Integer.TYPE, Integer.TYPE, Integer.TYPE);
		craftPlayer = getCBClass("entity.CraftPlayer");
		methodGetHandle = getMethod("getHandle", craftPlayer);
		methodBreakBlock = getMethod("breakBlock", playerInteractManager, blockPosition);
	}
	
	private static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(name, paramTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			System.out.println("Could not find method " + name + " in " + clazz.getSimpleName());
			wasSuccessful = false;
		}
		return null;
	}
	
	private static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			System.out.println("Could not find field " + name + " in " + clazz.getSimpleName());
			wasSuccessful = false;
		}
		return null;
	}
	
	private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) {
		try {
			return clazz.getConstructor(parameters);
		} catch (NoSuchMethodException | SecurityException e) {
			System.out.println("Could not find constructor for class " + clazz.getSimpleName());
			wasSuccessful = false;
		}
		return null;
	}
	
	private static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + version + className);
		} catch (Exception e) {
			System.out.println("Could not find class " + className);
			wasSuccessful = false;
		}
		return null;
	}
	
	private static Class<?> getCBClass(String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + version + className);
		} catch (Exception e) {
			System.out.println("Could not find class " + className);
			wasSuccessful = false;
		}
		return null;
	}
	
}