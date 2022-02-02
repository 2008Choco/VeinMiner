package wtf.choco.veinminer.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a hook for an anti cheat plugin. Implementations of this hook should exempt and
 * unexempt players in the respective methods.
 */
public interface AntiCheatHook {

    /**
     * Get the name of the plugin representing this hook.
     *
     * @return the name of this hook's plugin
     */
    @NotNull
    public String getPluginName();

    /**
     * Exempt a player from a fast-break check in the hooked anti cheat.
     *
     * @param player the player to exempt
     */
    public void exempt(@NotNull Player player);

    /**
     * Unexempt a player from a fast-break check in the hooked anti cheat.
     *
     * @param player the player to unexempt
     */
    public void unexempt(@NotNull Player player);

    /**
     * Check whether the provided player should be unexempted. This is a special-case method used to
     * check if players should be unexempted under certain situations. For example, in the case of
     * {@link AntiCheatHookNCP}, this method returns false if the player was exempted prior to the
     * execution of {@link #exempt(Player)} and should not be unexempted.
     * <p>
     * For anti cheats where temporary exemptions can be made, this method is not relevant and should
     * be left implemented as default.
     *
     * @param player the player to check
     *
     * @return true if should unexempt, false otherwise
     */
    public default boolean shouldUnexempt(@NotNull Player player) {
        return true;
    }

    /**
     * Check whether or not this anti cheat hook is supported. This should return false if, for
     * example in AntiAura, an API method to exempt players was added in a later version of the
     * anti cheat plugin
     *
     * @return true if supported, false otherwise
     */
    public default boolean isSupported() {
        return true;
    }

    /**
     * Attempt to fetch the {@link Plugin} instance for this hook.
     *
     * @return the plugin. null if could not be found
     */
    @Nullable
    public default Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(getPluginName());
    }

    /**
     * Represents a result for an anti cheat registration.
     */
    public enum RegistrationResult {

        /**
         * The registration succeeded.
         */
        SUCCESS,

        /**
         * An anti cheat hook with the given plugin name was already registered.
         */
        ALREADY_REGISTERED,

        /**
         * The anti cheat is unsupported (according to {@link AntiCheatHook#isSupported()}).
         */
        UNSUPPORTED;

        /**
         * Check whether or not the registration was a success.
         *
         * @return true if a success, false if it failed
         */
        public boolean isSuccess() {
            return this == SUCCESS;
        }

    }

}
