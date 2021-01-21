package wtf.choco.veinminer.anticheat;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;

/**
 * The default AntiAura hook implementation
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {

    private Object antiAuraApi;

    private final Method methodIsExemptedFromFastBreak;
    private final Method methodToggleExemptFromFastBreak;

    private final @NotNull VeinMiner plugin;
    private final Set<@NotNull UUID> exempt = new HashSet<>();

    private boolean compatible;

    public AntiCheatHookAntiAura(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.compatible = true;

        try {
            this.antiAuraApi = FieldUtils.getField(ClassUtils.getClass("AntiAuraAPI"), "API").get(null);
        } catch (ReflectiveOperationException e) {
            this.sendIncompatibleMessage();
        }

        this.methodIsExemptedFromFastBreak = MethodUtils.getAccessibleMethod(antiAuraApi.getClass(), "isExemptedFromFastBreak", Player.class);
        this.methodToggleExemptFromFastBreak = MethodUtils.getAccessibleMethod(antiAuraApi.getClass(), "toggleExemptFromFastBreak", Player.class);

        if (this.methodIsExemptedFromFastBreak == null || this.methodToggleExemptFromFastBreak == null) {
            this.sendIncompatibleMessage();
        }
    }

    /**
     * Checks whether the hook is in a compatible state.
     * eg. none of the methods are null.
     *
     * @return true if hook is in an incompatible state.
     */
    private boolean isIncompatible() {
        return !compatible || methodToggleExemptFromFastBreak == null || methodIsExemptedFromFastBreak == null;
    }

    private void sendIncompatibleMessage() {
        this.plugin.getLogger().severe("The version of " + getPluginName() + " on this server is incompatible with Veinminer. Please post information on the spigot resource discussion page.");
        this.compatible = false;
    }

    @NotNull
    @Override
    public String getPluginName() {
        return "AntiAura";
    }

    @Override
    public void exempt(@NotNull Player player) {
        try {
            if (isIncompatible() || Boolean.TRUE.equals(methodIsExemptedFromFastBreak.invoke(antiAuraApi, player))) {
                return;
            }

            if (exempt.add(player.getUniqueId())) {
                this.methodToggleExemptFromFastBreak.invoke(antiAuraApi, player);
            }
        } catch (ReflectiveOperationException e) {
            this.sendIncompatibleMessage();
        }
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (isIncompatible()) {
            return;
        }

        if (exempt.remove(player.getUniqueId())) {
            try {
                this.methodToggleExemptFromFastBreak.invoke(antiAuraApi, player);
            } catch (ReflectiveOperationException e) {
                this.sendIncompatibleMessage();
            }
        }
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

}
