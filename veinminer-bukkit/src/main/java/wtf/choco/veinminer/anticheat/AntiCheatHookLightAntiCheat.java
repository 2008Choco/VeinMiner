package wtf.choco.veinminer.anticheat;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.vekster.lightanticheat.api.CheckType;
import me.vekster.lightanticheat.api.DetectionStatus;
import me.vekster.lightanticheat.api.LACApi;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The default LightAntiCheat hook implementation.
 */
public final class AntiCheatHookLightAntiCheat implements AntiCheatHook {

    private final Multimap<UUID, String> exempt = MultimapBuilder.hashKeys().arrayListValues().build();

    @Override
    public void exempt(@NotNull Player player) {
        List<String> checks = new ArrayList<>();

        LACApi api = LACApi.getInstance();
        for (String checkName : api.getCheckNames(CheckType.ALL)) {
            if (api.getDetectionStatus(player, checkName) == DetectionStatus.ENABLED) {
                api.disableDetection(player, checkName);
                checks.add(checkName);
            }
        }

        this.exempt.putAll(player.getUniqueId(), checks);
    }

    @Override
    public void unexempt(@NotNull Player player) {
        LACApi api = LACApi.getInstance();
        for (String checkName : exempt.removeAll(player.getUniqueId())) {
            api.enableDetection(player, checkName);
        }
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.containsKey(player.getUniqueId());
    }

}
