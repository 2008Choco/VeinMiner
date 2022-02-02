package wtf.choco.veinminer.anticheat;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.reflect.MethodUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * The default LightAntiCheat hook implementation.
 */
public final class AntiCheatHookLightAntiCheat implements AntiCheatHook {

    private Class<?> lightAntiCheatApiClass;
    private Method methodIsApiBypass, methodSetApiBypass;

    private boolean supported;

    private final Set<UUID> exempt = new HashSet<>();

    public AntiCheatHookLightAntiCheat(VeinMinerPlugin plugin) {
        try {
            this.lightAntiCheatApiClass = Class.forName("vekster.lightanticheat.api.Utils");
            this.methodIsApiBypass = MethodUtils.getAccessibleMethod(lightAntiCheatApiClass, "isApiBypass", Player.class);
            this.methodSetApiBypass = MethodUtils.getAccessibleMethod(lightAntiCheatApiClass, "setApiBypass", new Class[] {
                Player.class, Boolean.TYPE
            });

            this.supported = (methodIsApiBypass != null && methodSetApiBypass != null);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("The version of " + getPluginName() + " on this server is incompatible with Veinminer. Please post information on the spigot resource discussion page.");
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public String getPluginName() {
        return "LightAntiCheat";
    }

    @Override
    public void exempt(@NotNull Player player) {
        try {
            if (Boolean.TRUE.equals(methodIsApiBypass.invoke(null, player))) {
                return;
            }

            if (exempt.add(player.getUniqueId())) {
                this.methodSetApiBypass.invoke(null, player, true);
            }
        } catch (ReflectiveOperationException e) { } // Ignore
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (!exempt.remove(player.getUniqueId())) {
            return;
        }

        try {
            this.methodSetApiBypass.invoke(null, player, false);
        } catch (ReflectiveOperationException e) { } // Ignore
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

}
