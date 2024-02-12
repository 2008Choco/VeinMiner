package wtf.choco.veinminer.listener;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.jetbrains.annotations.NotNull;

/**
 * A simple class tracking experience values to later spawn them at a specific {@link Location}.
 */
public final class ExperienceTracker {

    private List<Integer> experienceValues;

    /**
     * Construct a new {@link ExperienceTracker}.
     */
    ExperienceTracker() { }

    /**
     * Push an experience value to this tracker.
     *
     * @param experience the experience value
     */
    public void pushExperience(int experience) {
        Preconditions.checkArgument(experience > 0, "experience must be > 0, was %s", experience);

        if (experienceValues == null) {
            this.experienceValues = new ArrayList<>();
        }

        this.experienceValues.add(experience);
    }

    /**
     * Check whether or not the tracker has tracked any experience.
     *
     * @return true if there is at least one experience value, false if there are none
     */
    public boolean hasExperience() {
        return experienceValues != null;
    }

    /**
     * Spawn a series of experience orbs at the given {@link Location} matching the amount and
     * values pushed to this tracker.
     *
     * @param location the location at which to spawn the experience orbs
     */
    public void spawnExperienceOrbsAt(@NotNull Location location) {
        World world = location.getWorld();
        Preconditions.checkArgument(world != null, "location.getWorld() must not be null");

        this.experienceValues.forEach(experience -> world.spawn(location, ExperienceOrb.class, orb -> orb.setExperience(experience)));
    }

}
