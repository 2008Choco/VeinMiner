package wtf.choco.veinminer.anticheat;

import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.AACExemption;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The default Advanced AntiCheat (AAC) hook implementation.
 */
public final class AntiCheatHookAAC implements AntiCheatHook {

    private final AACAPI api;
    private final AACExemption exemption = new AACExemption("The player is using VeinMiner");

    public AntiCheatHookAAC() {
        this.api = Bukkit.getServicesManager().load(AACAPI.class);

        if (api == null) {
            throw new IllegalStateException("Tried to initialize " + getClass().getName() + " but couldn't find AACAPI");
        }
    }

    @Override
    public void exempt(@NotNull Player player) {
        this.api.addExemption(player, exemption);
    }

    @Override
    public void unexempt(@NotNull Player player) {
        this.api.removeExemption(player, exemption);
    }

}
