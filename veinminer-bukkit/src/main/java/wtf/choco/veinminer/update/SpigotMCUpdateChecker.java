package wtf.choco.veinminer.update;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link UpdateChecker} implementation that queries the SpigotMC API.
 */
public final class SpigotMCUpdateChecker implements UpdateChecker {

    private static final String USER_AGENT = "CHOCO-update-checker";
    private static final String UPDATE_URL = "https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=%d";

    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(5);

    private UpdateResult lastResult = null;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final JavaPlugin plugin;
    private final int pluginId;
    private final Gson gson = new Gson();

    /**
     * Construct a new {@link SpigotMCUpdateChecker}.
     *
     * @param plugin the plugin instance
     * @param pluginId the id of the plugin (found on the SpigotMC website)
     */
    public SpigotMCUpdateChecker(@NotNull JavaPlugin plugin, int pluginId) {
        this.plugin = plugin;
        this.pluginId = pluginId;
    }

    @NotNull
    @Override
    public Optional<UpdateResult> getLastUpdateResult() {
        return Optional.ofNullable(lastResult);
    }

    @NotNull
    @Override
    public CompletableFuture<UpdateResult> checkForUpdates(@NotNull VersionScheme versionScheme) {
        String currentVersion = plugin.getDescription().getVersion();

        return httpClient.sendAsync(
                HttpRequest.newBuilder()
                    .GET().uri(URI.create(UPDATE_URL.formatted(pluginId)))
                    .header("User-Agent", USER_AGENT)
                    .timeout(TIMEOUT_DURATION)
                    .build(),
                BodyHandlers.ofString()
        ).thenApply(response -> {
            int statusCode = response.statusCode();
            String body = response.body();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new UpdateFailException(statusCode, body);
            }

            JsonObject object = gson.fromJson(body, JsonObject.class);

            String fetchedVersion = object.get("current_version").getAsString();
            int compare = versionScheme.compareVersions(currentVersion, fetchedVersion);

            UpdateResult result;
            if (compare > 0) {
                result = UpdateResult.unreleased(currentVersion, fetchedVersion, versionScheme);
            } else if (compare < 0) {
                result = UpdateResult.updateAvailable(currentVersion, fetchedVersion, versionScheme);
            } else {
                result = UpdateResult.upToDate(currentVersion, versionScheme);
            }

            this.lastResult = result;
            return result;
        }).exceptionally(e -> UpdateResult.failed(currentVersion, versionScheme, e));
    }

}
