package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The default AntiAura hook implementation
 */
public final class AntiCheatHookAntiAura implements AntiCheatHook {

    private final double version;
    private final Set<UUID> exempted = new HashSet<>();

    public AntiCheatHookAntiAura() {
        Plugin antiaura = Bukkit.getPluginManager().getPlugin("AntiAura");
        if (antiaura == null) {
            throw new UnsupportedOperationException("Attempted to construct AntiAura hook while AntiAura is not installed");
        }

        this.version = NumberUtils.toDouble(antiaura.getDescription().getVersion(), -1.0);
    }

    @Override
    @NotNull
    public String getPluginName() {
        return "AntiAura";
    }

    @Override
    public void exempt(@NotNull Player player) {
        if (AntiAuraAPI.API.isExemptedFromFastBreak(player)) return;

        AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        this.exempted.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        AntiAuraAPI.API.toggleExemptFromFastBreak(player);
        this.exempted.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempted.contains(player.getUniqueId());
    }

    @Override
    public boolean isSupported() {
        return version >= 10.83;
    }

}