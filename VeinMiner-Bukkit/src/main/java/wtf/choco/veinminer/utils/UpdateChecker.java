package wtf.choco.veinminer.utils;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class to assist in checking for updates for plugins uploaded to
 * <a href="https://spigotmc.org/resources/">SpigotMC</a>. Before any members of this
 * class are accessed, {@link #init(JavaPlugin, int)} must be invoked by the plugin,
 * preferrably in its {@link JavaPlugin#onEnable()} method, though that is not a
 * requirement.
 * <p>
 * This class performs asynchronous queries to Spigot's API. If the results of
 * {@link #requestUpdateCheck()} are inconsistent with what is published on SpigotMC, it
 * may be due to the REST API cache. Results will be updated in due time.
 *
 * @author Parker Hawke - Choco
 */
public final class UpdateChecker {

    public static final VersionScheme VERSION_SCHEME_DECIMAL = (first, second) -> {
        String[] firstSplit = splitVersionInfo(first), secondSplit = splitVersionInfo(second);
        if (firstSplit == null || secondSplit == null) {
            return null;
        }

        for (int i = 0; i < Math.min(firstSplit.length, secondSplit.length); i++) {
            int currentValue = NumberUtils.toInt(firstSplit[i]), newestValue = NumberUtils.toInt(secondSplit[i]);

            if (newestValue > currentValue) {
                return second;
            } else if (newestValue < currentValue) {
                return first;
            }
        }

        return (secondSplit.length > firstSplit.length) ? second : first;
    };

    private static final String USER_AGENT = "CHOCO-update-checker";
    private static final String UPDATE_URL = "https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=%d";
    private static final Pattern DECIMAL_SCHEME_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)*");

    private static UpdateChecker instance;

    private UpdateResult lastResult = null;

    private final JavaPlugin plugin;
    private final int pluginID;
    private final VersionScheme versionScheme;

    private UpdateChecker(@NotNull JavaPlugin plugin, int pluginID, @NotNull VersionScheme versionScheme) {
        this.plugin = plugin;
        this.pluginID = pluginID;
        this.versionScheme = versionScheme;
    }

    /**
     * Request an update check to Spigot. This request is asynchronous and may not
     * complete immediately as an HTTP GET request is published to the Spigot API.
     *
     * @return a future update result
     */
    @NotNull
    public CompletableFuture<@NotNull UpdateResult> requestUpdateCheck() {
        return CompletableFuture.supplyAsync(() -> {
            int responseCode = -1;

            try {
                URL url = new URL(String.format(UPDATE_URL, pluginID));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-Agent", USER_AGENT);
                responseCode = connection.getResponseCode();

                JsonParser parser = new JsonParser();
                JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
                JsonElement json = parser.parse(reader);
                reader.close();

                if (!json.isJsonObject()) {
                    return new UpdateResult(UpdateReason.INVALID_JSON);
                }

                String currentVersion = json.getAsJsonObject().get("current_version").getAsString();
                String pluginVersion = plugin.getDescription().getVersion();
                String latest = versionScheme.compareVersions(pluginVersion, currentVersion);

                if (latest == null) {
                    return new UpdateResult(UpdateReason.UNSUPPORTED_VERSION_SCHEME);
                }
                else if (latest.equals(pluginVersion)) {
                    return new UpdateResult(pluginVersion.equals(currentVersion) ? UpdateReason.UP_TO_DATE : UpdateReason.UNRELEASED_VERSION);
                }
                else if (latest.equals(currentVersion)) {
                    return new UpdateResult(UpdateReason.NEW_UPDATE, latest);
                }
            } catch (IOException e) {
                return new UpdateResult(UpdateReason.COULD_NOT_CONNECT);
            }

            return new UpdateResult(responseCode == 401 ? UpdateReason.UNAUTHORIZED_QUERY : UpdateReason.UNKNOWN_ERROR);
        });
    }

    /**
     * Get the last update result that was queried by {@link #requestUpdateCheck()}. If no
     * update check was performed since this class' initialization, this method will
     * return null.
     *
     * @return the last update check result. null if none.
     */
    @Nullable
    public UpdateResult getLastResult() {
        return lastResult;
    }

    private static String[] splitVersionInfo(String version) {
        Matcher matcher = DECIMAL_SCHEME_PATTERN.matcher(version);
        return matcher.find() ? matcher.group().split("\\.") : null;
    }

    /**
     * Initialize this update checker with the specified values and return its instance.
     * If an instance of UpdateChecker has already been initialized, this method will act
     * similarly to {@link #get()} (which is recommended after initialization).
     *
     * @param plugin the plugin for which to check updates. Cannot be null
     * @param pluginID the ID of the plugin as identified in the SpigotMC resource link.
     * For example, "https://www.spigotmc.org/resources/veinminer.<b>12038</b>/" would
     * expect "12038" as a value. The value must be greater than 0
     * @param versionScheme a custom version scheme parser. Cannot be null
     *
     * @return the UpdateChecker instance
     */
    public static UpdateChecker init(@NotNull JavaPlugin plugin, int pluginID, @NotNull VersionScheme versionScheme) {
        Preconditions.checkArgument(plugin != null, "Plugin cannot be null");
        Preconditions.checkArgument(pluginID > 0, "Plugin ID must be greater than 0");
        Preconditions.checkArgument(versionScheme != null, "null version schemes are unsupported");

        return (instance == null) ? instance = new UpdateChecker(plugin, pluginID, versionScheme) : instance;
    }

    /**
     * Initialize this update checker with the specified values and return its instance.
     * If an instance of UpdateChecker has already been initialized, this method will act
     * similarly to {@link #get()} (which is recommended after initialization).
     *
     * @param plugin the plugin for which to check updates. Cannot be null
     * @param pluginID the ID of the plugin as identified in the SpigotMC resource link.
     * For example, "https://www.spigotmc.org/resources/veinminer.<b>12038</b>/" would
     * expect "12038" as a value. The value must be greater than 0
     *
     * @return the UpdateChecker instance
     */
    public static UpdateChecker init(@NotNull JavaPlugin plugin, int pluginID) {
        return init(plugin, pluginID, VERSION_SCHEME_DECIMAL);
    }

    /**
     * Get the initialized instance of UpdateChecker. If {@link #init(JavaPlugin, int)}
     * has not yet been invoked, this method will throw an exception.
     *
     * @return the UpdateChecker instance
     */
    @NotNull
    public static UpdateChecker get() {
        Preconditions.checkState(instance != null, "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }

    /**
     * Check whether the UpdateChecker has been initialized or not (if
     * {@link #init(JavaPlugin, int)} has been invoked) and {@link #get()} is safe to use.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return instance != null;
    }


    /**
     * A functional interface to compare two version Strings with similar version schemes.
     */
    @FunctionalInterface
    public static interface VersionScheme {

        /**
         * Compare two versions and return the higher of the two. If null is returned, it
         * is assumed that at least one of the two versions are unsupported by this
         * version scheme parser.
         *
         * @param first the first version to check
         * @param second the second version to check
         *
         * @return the greater of the two versions. null if unsupported version schemes
         */
        @Nullable
        public String compareVersions(@NotNull String first, @NotNull String second);

    }

    /**
     * A constant reason for the result of {@link UpdateResult}.
     */
    public static enum UpdateReason {

        /**
         * A new update is available for download on SpigotMC.
         */
        NEW_UPDATE, // The only reason that requires an update

        /**
         * A successful connection to the Spigot API could not be established.
         */
        COULD_NOT_CONNECT,

        /**
         * The JSON retrieved from Spigot was invalid or malformed.
         */
        INVALID_JSON,

        /**
         * A 401 error was returned by the Spigot API.
         */
        UNAUTHORIZED_QUERY,

        /**
         * The version of the plugin installed on the server is greater than the one
         * uploaded to SpigotMC's resources section.
         */
        UNRELEASED_VERSION,

        /**
         * An unknown error occurred.
         */
        UNKNOWN_ERROR,

        /**
         * The plugin uses an unsupported version scheme, therefore a proper comparison
         * between versions could not be made.
         */
        UNSUPPORTED_VERSION_SCHEME,

        /**
         * The plugin is up to date with the version released on SpigotMC's resources
         * section.
         */
        UP_TO_DATE;

    }

    /**
     * Represents a result for an update query performed by
     * {@link UpdateChecker#requestUpdateCheck()}.
     */
    public final class UpdateResult {

        private final UpdateReason reason;
        private final String newestVersion;

        { // An actual use for initializer blocks. This is madness!
            UpdateChecker.this.lastResult = this;
        }

        private UpdateResult(@NotNull UpdateReason reason, @NotNull String newestVersion) {
            this.reason = reason;
            this.newestVersion = newestVersion;
        }

        private UpdateResult(@NotNull UpdateReason reason) {
            Preconditions.checkArgument(reason != UpdateReason.NEW_UPDATE, "Reasons that require updates must also provide the latest version String");

            this.reason = reason;
            this.newestVersion = plugin.getDescription().getVersion();
        }

        /**
         * Get the constant reason of this result.
         *
         * @return the reason
         */
        @NotNull
        public UpdateReason getReason() {
            return reason;
        }

        /**
         * Check whether or not this result requires the user to update.
         *
         * @return true if requires update, false otherwise
         */
        public boolean requiresUpdate() {
            return reason == UpdateReason.NEW_UPDATE;
        }

        /**
         * Get the latest version of the plugin. This may be the currently installed version, it
         * may not be. This depends entirely on the result of the update.
         *
         * @return the newest version of the plugin
         */
        @NotNull
        public String getNewestVersion() {
            return newestVersion;
        }

    }

}
