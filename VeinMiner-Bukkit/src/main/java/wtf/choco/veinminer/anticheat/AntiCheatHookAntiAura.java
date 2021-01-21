package wtf.choco.veinminer.anticheat;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wtf.choco.veinminer.VeinMiner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The default AntiAura hook implementation
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {
    private @Nullable Object antiAuraApi;
    private @Nullable Method isExemptedFromFastBreak;
    private @Nullable Method toggleExemptFromFastBreak;

    private final @NotNull VeinMiner veinMiner;
    private final Set<@NotNull UUID> exempt = new HashSet<>();

    private boolean compatible;

    public AntiCheatHookAntiAura(@NotNull VeinMiner veinMiner) {
        //It is assumed that antiaura is on the server if "AntiAura" plugin is enabled.

        this.veinMiner = veinMiner;
        this.compatible = true;

        try {
            this.antiAuraApi = FieldUtils.getField(ClassUtils.getClass("AntiAuraAPI"), "API").get(null);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            this.sendIncompatibleMessage();
            return;
        }

        this.isExemptedFromFastBreak = MethodUtils.getAccessibleMethod(
                this.antiAuraApi.getClass(),
                "isExemptedFromFastBreak",
                new Class<?>[] { Player.class }
        );

        this.toggleExemptFromFastBreak = MethodUtils.getAccessibleMethod(
                this.antiAuraApi.getClass(),
                "toggleExemptFromFastBreak",
                new Class<?>[] { Player.class }
        );

        if (this.isExemptedFromFastBreak == null || this.toggleExemptFromFastBreak == null) {
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
        return !this.compatible
                || this.toggleExemptFromFastBreak == null
                || this.isExemptedFromFastBreak == null;
    }

    private void sendIncompatibleMessage() {
        this.veinMiner.getLogger().severe(() ->
                String.format(
                        "The version of %s on this server is incompatible with Veinminer."
                                + "Please post information on the spigot resource discussion page.",
                        this.getPluginName()
                )
        );
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
            if (this.isIncompatible()
                    || Boolean.TRUE.equals(this.isExemptedFromFastBreak.invoke(this.antiAuraApi, player))) {
                return;
            }

            if (this.exempt.add(player.getUniqueId())) {
                this.toggleExemptFromFastBreak.invoke(this.antiAuraApi, player);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            this.sendIncompatibleMessage();
        }
    }

    @Override
    public void unexempt(@NotNull Player player) {
        if (this.isIncompatible()) {
            return;
        }

        if (this.exempt.remove(player.getUniqueId())) {
            try {
                this.toggleExemptFromFastBreak.invoke(this.antiAuraApi, player);
            } catch (IllegalAccessException | InvocationTargetException e) {
                this.sendIncompatibleMessage();
            }
        }
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

}
