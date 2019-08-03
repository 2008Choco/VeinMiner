package wtf.choco.veinminer.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents various configurable options for the vein miner algorithm. These options may be
 * applied globally, per-category and per-template.
 *
 * @author Parker Hawke - Choco
 */
public final class AlgorithmConfig implements Cloneable {

    private static final boolean DEFAULT_REPAIR_FRIENDLY_VEINMINER = false, DEFAULT_INCLUDE_EDGES = true;
    private static final int DEFAULT_MAX_VEIN_SIZE = 64;
    private static final double DEFAULT_COST = 0.0;

    private Boolean repairFriendlyVeinMiner, includeEdges;
    private Integer maxVeinSize;
    private Double cost;
    private final Set<UUID> disabledWorlds = new HashSet<>();

    private AlgorithmConfig parent;

    /**
     * Construct an algorithm config based on the values of an existing config. This is equivalent
     * to calling {@link #clone()}.
     *
     * @param parent the parent algorithm config. null if this is the root configuration
     */
    public AlgorithmConfig(@Nullable AlgorithmConfig parent) {
        this.parent = parent;
    }

    public AlgorithmConfig() {
        this(null);
    }

    /**
     * Set the parent algorithm config from which to retrieve values if not defined within this
     * scope.
     *
     * @param parent the config parent. null if none
     */
    public void setParent(@Nullable AlgorithmConfig parent) {
        this.parent = parent;
    }

    /**
     * Set whether or not vein miner should be repair-friendly. If true, vein miner will
     * cease execution before the tool has lost all its durability allowing the user to
     * repair their tool before it gets broken. If false, vein miner will cease execution
     * after the tool has broken.
     *
     * @param friendly the repair-friendly state
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig repairFriendly(boolean friendly) {
        this.repairFriendlyVeinMiner = friendly;
        return this;
    }

    /**
     * Get whether or not vein miner should be repair-friendly. See
     * {@link #repairFriendly(boolean)}.
     *
     * @return true if repair-friendly, false otherwise
     */
    public boolean isRepairFriendly() {
        return (repairFriendlyVeinMiner != null) ? repairFriendlyVeinMiner.booleanValue() : (parent != null ? parent.isRepairFriendly() : DEFAULT_REPAIR_FRIENDLY_VEINMINER);
    }

    /**
     * Set whether or not vein miner should search for vein mineable blocks along the
     * farthest edges of a block (i.e. north north east, north north west, etc.).
     *
     * @param includeEdges whether to include edges
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig includeEdges(boolean includeEdges) {
        this.includeEdges = includeEdges;
        return this;
    }

    /**
     * Check whether or not vein miner should include edges. See {@link #includesEdges()}.
     *
     * @return true if includes edges, false otherwise
     */
    public boolean includesEdges() {
        return (includeEdges != null) ? includeEdges.booleanValue() : (parent != null ? parent.includesEdges() : DEFAULT_INCLUDE_EDGES);
    }

    /**
     * Set the maximum vein size.
     *
     * @param maxVeinSize the maximum vein size
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig maxVeinSize(int maxVeinSize) {
        Preconditions.checkArgument(maxVeinSize > 0, "Max vein size must be > 0");

        this.maxVeinSize = maxVeinSize;
        return this;
    }

    /**
     * Get the maximum vein size.
     *
     * @return the maximum vein size
     */
    public int getMaxVeinSize() {
        return (maxVeinSize != null) ? maxVeinSize.intValue() : (parent != null ? parent.getMaxVeinSize() : DEFAULT_MAX_VEIN_SIZE);
    }

    /**
     * Set the amount of money required to vein mine. Note, this feature requires Vault
     * and an economy plugin, else it is ignored.
     *
     * @param cost the cost
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig cost(double cost) {
        Preconditions.checkArgument(cost >= 0.0, "Cost must be positive or 0");

        this.cost = cost;
        return this;
    }

    /**
     * Get the cost.
     *
     * @return the cost
     */
    public double getCost() {
        return (cost != null) ? cost.doubleValue() : (parent != null ? parent.getCost() : DEFAULT_COST);
    }

    /**
     * Add a world in which vein miner should be disabled.
     *
     * @param world the world in which to disable vein miner
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig disabledWorld(@NotNull World world) {
        Preconditions.checkArgument(world != null, "Cannot disable null world");

        this.disabledWorlds.add(world.getUID());
        return this;
    }

    /**
     * Remove a world in which vein miner is disabled. Vein miner will be usable again.
     *
     * @param world the world in which to enabled vein miner
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig undisableWorld(@NotNull World world) {
        Preconditions.checkArgument(world != null, "Cannot undisable null world");

        this.disabledWorlds.remove(world.getUID());
        return this;
    }

    /**
     * Check whether or not vein miner is disabled in the specified world.
     *
     * @param world the world to check
     *
     * @return true if disabled, false otherwise
     */
    public boolean isDisabledWorld(@NotNull World world) {
        return world != null && (disabledWorlds.contains(world.getUID()) || (parent != null && parent.isDisabledWorld(world)));
    }

    /**
     * Copy all values from the provided configuration to this configuration. Any undefined values will
     * be overwritten from the provided configuration, as well as any undefined values from it will be
     * written to this instance. This method is the equivalent of calling {@link #clone()}, only no new
     * instance is created. This object acts as the modified instance.
     *
     * @param config the config from which to copy
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public AlgorithmConfig copyValues(@NotNull AlgorithmConfig config) {
        this.repairFriendlyVeinMiner = config.repairFriendlyVeinMiner;
        this.includeEdges = config.includeEdges;
        this.maxVeinSize = config.maxVeinSize;
        this.disabledWorlds.clear();
        this.disabledWorlds.addAll(config.disabledWorlds);

        return this;
    }

    /**
     * Assign all configured values to hard-coded defaults provided by VeinMiner.
     *
     * @return the default VeinMiner config
     */
    @NotNull
    public AlgorithmConfig defaultValues() {
        this.repairFriendlyVeinMiner = DEFAULT_REPAIR_FRIENDLY_VEINMINER;
        this.includeEdges = DEFAULT_INCLUDE_EDGES;
        this.maxVeinSize = DEFAULT_MAX_VEIN_SIZE;
        this.disabledWorlds.clear();

        return this;
    }

    @Override
    public AlgorithmConfig clone() {
        return new AlgorithmConfig(parent).copyValues(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repairFriendlyVeinMiner, includeEdges, maxVeinSize, disabledWorlds);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof AlgorithmConfig)) return false;

        AlgorithmConfig other = (AlgorithmConfig) obj;
        return repairFriendlyVeinMiner == other.repairFriendlyVeinMiner && includeEdges == other.includeEdges
            && maxVeinSize == other.maxVeinSize && Objects.equals(disabledWorlds, other.disabledWorlds);
    }

}