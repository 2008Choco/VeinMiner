package wtf.choco.veinminer.config;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple configuration that determines a client's ability to perform certain
 * abilities per the server's discretion.
 */
public final class ClientConfig implements Cloneable {

    private boolean allowActivationKeybind = true;
    private boolean allowPatternSwitchingKeybind = true;
    private boolean allowWireframeRendering = true;

    private ClientConfig(@NotNull ClientConfig config) {
        this.allowActivationKeybind = config.allowActivationKeybind;
        this.allowPatternSwitchingKeybind = config.allowPatternSwitchingKeybind;
        this.allowWireframeRendering = config.allowWireframeRendering;
    }

    /**
     * Construct a new {@link ClientConfig} with default values.
     */
    public ClientConfig() { }

    /**
     * Check whether or not the activation key bind is allowed.
     *
     * @return true if allowed, false otherwise
     */
    public boolean isAllowActivationKeybind() {
        return allowActivationKeybind;
    }

    /**
     * Check whether or not the pattern switching key binds are allowed.
     *
     * @return true if allowed, false otherwise
     */
    public boolean isAllowPatternSwitchingKeybind() {
        return allowPatternSwitchingKeybind;
    }

    /**
     * Check whether or not the client is allowed to render a wireframe around blocks
     * it intends on vein mining before actually vein mining.
     *
     * @return true if allowed, false otherwise
     */
    public boolean isAllowWireframeRendering() {
        return allowWireframeRendering;
    }

    /**
     * Get all boolean values from this config as a single byte bitmask.
     * <p>
     * 0x01: {@link #isAllowActivationKeybind()}<br>
     * 0x02: {@link #isAllowPatternSwitchingKeybind()}<br>
     * 0x04: {@link #isAllowWireframeRendering()}
     *
     * @return the config bitmask
     */
    public byte getBooleanValuesAsBitmask() {
        byte bitmask = 0;

        bitmask |= (allowActivationKeybind) ? 0x01 : 0x0;
        bitmask |= (allowPatternSwitchingKeybind) ? 0x02 : 0x0;
        bitmask |= (allowWireframeRendering) ? 0x04 : 0x0;

        return bitmask;
    }

    /**
     * Edit this {@link ClientConfig} with the given {@link Consumer} and return a new
     * instance of the config with all edited values. This operation is immutable and will
     * not modify this instance of the config.
     *
     * @param editor the editor
     *
     * @return the newly edited ClientConfig instance
     */
    @NotNull
    public ClientConfig edit(Consumer<ClientConfig.Builder> editor) {
        ClientConfig.Builder builder = new ClientConfig.Builder(this);
        editor.accept(builder);
        return builder.build();
    }

    /**
     * Get a new {@link Builder} instance to create a new {@link ClientConfig}.
     *
     * @return the builder instance
     */
    @NotNull
    public static ClientConfig.Builder builder() {
        return new ClientConfig.Builder();
    }

    @Override
    public ClientConfig clone() {
        return new ClientConfig(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowActivationKeybind, allowPatternSwitchingKeybind, allowWireframeRendering);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ClientConfig other)) {
            return false;
        }

        return allowActivationKeybind == other.allowActivationKeybind && allowPatternSwitchingKeybind == other.allowPatternSwitchingKeybind
                && allowWireframeRendering == other.allowWireframeRendering;
    }

    /**
     * A builder class for {@link ClientConfig} instances.
     */
    public static final class Builder {

        private final ClientConfig config;

        private Builder(@NotNull ClientConfig config) {
            this.config = config.clone();
        }

        private Builder() {
            this(new ClientConfig());
        }

        /**
         * Set whether or not the activation key bind is allowed.
         *
         * @param allowActivationKeybind true if allowed, false if not
         *
         * @return this instance. Allows for chained method calls
         *
         * @see ClientConfig#isAllowActivationKeybind()
         */
        @NotNull
        public Builder allowActivationKeybind(boolean allowActivationKeybind) {
            this.config.allowActivationKeybind = allowActivationKeybind;
            return this;
        }

        /**
         * Set whether or not the pattern switching key binds are allowed.
         *
         * @param allowPatternSwitchingKeybind true if allowed, false if not
         *
         * @return this instance. Allows for chained method calls
         *
         * @see ClientConfig#isAllowPatternSwitchingKeybind()
         */
        @NotNull
        public Builder allowPatternSwitchingKeybind(boolean allowPatternSwitchingKeybind) {
            this.config.allowPatternSwitchingKeybind = allowPatternSwitchingKeybind;
            return this;
        }

        /**
         * Set whether or not the client is allowed to render a wireframe.
         *
         * @param allowWireframeRendering true if allowed, false if not
         *
         * @return this instance. Allows for chained method calls
         *
         * @see ClientConfig#isAllowWireframeRendering()
         */
        @NotNull
        public Builder allowWireframeRendering(boolean allowWireframeRendering) {
            this.config.allowWireframeRendering = allowWireframeRendering;
            return this;
        }

        /**
         * Apply the given bitmask to this builder's boolean values.
         *
         * @param bitmask the bitmask
         *
         * @return this instance. Allows for chained method calls
         *
         * @see ClientConfig#getBooleanValuesAsBitmask()
         */
        @NotNull
        public Builder applyBitmask(byte bitmask) {
            this.allowActivationKeybind((bitmask & 0x01) != 0);
            this.allowPatternSwitchingKeybind((bitmask & 0x02) != 0);
            this.allowWireframeRendering((bitmask & 0x04) != 0);
            return this;
        }

        /**
         * Get the {@link ClientConfig} instance.
         *
         * @return the built instance
         */
        @NotNull
        public ClientConfig build() {
            return config;
        }

    }

}
