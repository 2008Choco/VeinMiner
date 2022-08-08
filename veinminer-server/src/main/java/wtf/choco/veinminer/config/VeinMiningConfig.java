package wtf.choco.veinminer.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Represents VeinMiner's configuration for vein mining operations.
 */
public final class VeinMiningConfig implements Cloneable {

    private boolean repairFriendly = false;
    private int maxVeinSize = 64;
    private double cost = 0.0D;

    private Set<String> disabledWorlds = new HashSet<>();

    // Reserved for cloning
    private VeinMiningConfig(@NotNull VeinMiningConfig config) {
        this.repairFriendly = config.repairFriendly;
        this.maxVeinSize = config.maxVeinSize;
        this.cost = config.cost;
        this.disabledWorlds = new HashSet<>(config.disabledWorlds);
    }

    /**
     * Construct a new default {@link VeinMiningConfig}.
     *
     * @see #builder()
     */
    public VeinMiningConfig() { }

    /**
     * Check whether or not repair friendly is enabled.
     * <p>
     * If {@code true}, VeinMiner will stop breaking blocks when a tool reaches 1
     * durability as to avoid breaking it.
     *
     * @return true if repair friendly, false otherwise
     */
    public boolean isRepairFriendly() {
        return repairFriendly;
    }

    /**
     * Get the maximum vein size.
     * <p>
     * The returned integer is the maximum amount of blocks VeinMiner will mine for
     * any given vein of blocks.
     *
     * @return the maximum vein size
     */
    public int getMaxVeinSize() {
        return maxVeinSize;
    }

    /**
     * Get the cost of using VeinMiner.
     * <p>
     * The returned double is the amount of money that will be taken from the player
     * if an economy is installed on the server.
     *
     * @return the cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Check whether or not the world with the given (case-sensitive) name is disabled.
     * <p>
     * Disabled worlds will disallow VeinMiner from being used at all.
     *
     * @param worldName the name of the world to check
     *
     * @return true if disabled, false otherwise
     */
    public boolean isDisabledWorld(@NotNull String worldName) {
        return disabledWorlds.contains(worldName);
    }

    /**
     * Get an unmodifiable {@link Set} of all disabled worlds.
     *
     * @return all disabled world names
     */
    @NotNull
    @UnmodifiableView
    public Set<String> getDisabledWorlds() {
        return Collections.unmodifiableSet(disabledWorlds);
    }

    /**
     * Edit this {@link VeinMiningConfig} with the given {@link Consumer} and return a new
     * instance of the config with all edited values. This operation is immutable and will
     * not modify this instance of the config.
     *
     * @param editor the editor
     *
     * @return the newly edited VeinMinerConfig instance
     */
    @NotNull
    public VeinMiningConfig edit(@NotNull Consumer<VeinMiningConfig.Builder> editor) {
        VeinMiningConfig.Builder builder = new VeinMiningConfig.Builder(this);
        editor.accept(builder);
        return builder.build();
    }

    /**
     * Get a new builder for a {@link VeinMiningConfig}.
     *
     * @return a builder
     *
     * @see VeinMiningConfig.Builder
     */
    @NotNull
    public static VeinMiningConfig.Builder builder() {
        return new VeinMiningConfig.Builder();
    }

    @NotNull
    @Override
    public VeinMiningConfig clone() {
        return new VeinMiningConfig(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repairFriendly, maxVeinSize, cost, disabledWorlds);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof VeinMiningConfig other)) {
            return false;
        }

        return repairFriendly == other.repairFriendly
                && maxVeinSize == other.maxVeinSize
                && cost == other.cost
                && Objects.equals(disabledWorlds, other.disabledWorlds);
    }

    /**
     * A builder for a {@link VeinMiningConfig}.
     */
    public static final class Builder {

        private final VeinMiningConfig config;

        private Builder(@NotNull VeinMiningConfig config) {
            this.config = config.clone();
        }

        private Builder() {
            this(new VeinMiningConfig());
        }

        /**
         * Set whether or not this configuration is repair friendly.
         *
         * @param repairFriendly the value
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#isRepairFriendly()
         */
        @NotNull
        public Builder repairFriendly(boolean repairFriendly) {
            this.config.repairFriendly = repairFriendly;
            return this;
        }

        /**
         * Set the maximum vein size.
         *
         * @param maxVeinSize the value
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#getMaxVeinSize()
         */
        @NotNull
        public Builder maxVeinSize(int maxVeinSize) {
            this.config.maxVeinSize = maxVeinSize;
            return this;
        }

        /**
         * Set the cost of vein mining.
         *
         * @param cost the cost
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#getCost()
         */
        @NotNull
        public Builder cost(double cost) {
            this.config.cost = cost;
            return this;
        }

        /**
         * Disable the world with the given name.
         *
         * @param worldName the name of the world to disable
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#isDisabledWorld(String)
         */
        @NotNull
        public Builder disableWorld(@NotNull String worldName) {
            this.config.disabledWorlds.add(worldName);
            return this;
        }

        /**
         * Disable multiple worlds with the given names.
         *
         * @param worldNames an iterable containing all world names to disable
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#isDisabledWorld(String)
         */
        @NotNull
        public Builder disableWorlds(@NotNull Iterable<String> worldNames) {
            worldNames.forEach(config.disabledWorlds::add);
            return this;
        }

        /**
         * Undisable the world with the given name (if it was disabled previously).
         *
         * @param worldName the name of the world to undisable
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#isDisabledWorld(String)
         */
        @NotNull
        public Builder undisableWorld(@NotNull String worldName) {
            this.config.disabledWorlds.remove(worldName);
            return this;
        }

        /**
         * Clear all disabled worlds (if any were disabled previously).
         *
         * @return this instance. Allows for chained method calls
         *
         * @see VeinMiningConfig#isDisabledWorld(String)
         */
        @NotNull
        public Builder clearDisabledWorlds() {
            this.config.disabledWorlds.clear();
            return this;
        }

        /**
         * Build and return the {@link VeinMiningConfig}.
         *
         * @return the config
         */
        @NotNull
        public VeinMiningConfig build() {
            return config;
        }

    }

}
