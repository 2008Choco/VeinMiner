package wtf.choco.veinminer.anticheat;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

import top.polar.api.PolarApi;
import top.polar.api.PolarApiAccessor;
import top.polar.api.exception.PolarNotLoadedException;
import top.polar.api.loader.LoaderApi;
import top.polar.api.user.event.DetectionAlertEvent;
import top.polar.api.user.event.type.CheckType;

/**
 * The default Polar AntiCheat hook implementation.
 */
public final class AntiCheatHookPolar implements AntiCheatHook {

    private final VeinMinerPlugin plugin;
    private final Set<UUID> exempt = Sets.newConcurrentHashSet(); // Polar does some really strange classloading. I can't reasonably guarantee its events are on the main thread

    public AntiCheatHookPolar(VeinMinerPlugin plugin) {
        this.plugin = plugin;
        LoaderApi.registerEnableCallback(this::initialize);
    }

    private void initialize() {
        PolarApi api;
        try {
            api = PolarApiAccessor.access().get();
        } catch (PolarNotLoadedException e) {
            this.plugin.getLogger().warning("Failed to initialize support for Polar AntiCheat. This is a bug. Please report this exception to VeinMiner!");
            e.printStackTrace();
            return;
        }

        api.events().repository().registerListener(DetectionAlertEvent.class, this::onDetectionAlert);
    }

    @Override
    public void exempt(@NotNull Player player) {
        this.exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        this.exempt.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

    private void onDetectionAlert(DetectionAlertEvent event) {
        if (!exempt.contains(event.user().uuid())) {
            return;
        }

        if (event.check().type() == CheckType.BLOCK_INTERACT) {
            event.cancelled(true);
        }
    }

}
