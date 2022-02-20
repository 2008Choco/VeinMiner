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
 * The default AntiAura hook implementation.
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {

    private Class<?> antiAuraApiClass;
    private Method methodIsExemptedFromFastBreak;
    private Method methodToggleExemptFromFastBreak;

    private boolean supported;

    private final Set<UUID> exempt = new HashSet<>();

    public AntiCheatHookAntiAura(@NotNull VeinMinerPlugin plugin) {
        try {
            this.antiAuraApiClass = Class.forName("AntiAuraAPI.API");
            this.methodIsExemptedFromFastBreak = MethodUtils.getAccessibleMethod(antiAuraApiClass, "isExemptedFromFastBreak", Player.class);
            this.methodToggleExemptFromFastBreak = MethodUtils.getAccessibleMethod(antiAuraApiClass, "toggleExemptFromFastBreak", Player.class);

            this.supported = (methodToggleExemptFromFastBreak != null && methodIsExemptedFromFastBreak != null);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("The version of AntiAura on this server is incompatible with Veinminer. Please post information on the spigot resource discussion page.");
            e.printStackTrace();
        }
    }

    @Override
    public void exempt(@NotNull Player player) {
        try {
            if (Boolean.TRUE.equals(methodIsExemptedFromFastBreak.invoke(antiAuraApiClass, player))) {
                return;
            }

            if (exempt.add(player.getUniqueId())) {
                this.methodToggleExemptFromFastBreak.invoke(antiAuraApiClass, player);
            }
        } catch (ReflectiveOperationException e) { } // Ignore
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (!exempt.remove(player.getUniqueId())) {
            return;
        }

        try {
            this.methodToggleExemptFromFastBreak.invoke(antiAuraApiClass, player);
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
