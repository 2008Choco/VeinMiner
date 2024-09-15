package wtf.choco.veinminer.language;

import com.google.common.base.Enums;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link LanguageFile} backed by a JSON file.
 */
public final class JsonLanguageFile implements LanguageFile {

    private static final Pattern COLOR_PATTERN = Pattern.compile("<(\\w+)>");

    private Map<String, String> messages;

    private final Path filePath;

    /**
     * Construct a new {@link LanguageFile} at the given path.
     *
     * @param filePath the file path. Must end with .json
     */
    public JsonLanguageFile(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public String get(String key) {
        String message = messages.get(key);

        if (message != null) {
            message = colorize(message);
        }

        return message;
    }

    @Override
    public String get(String key, Object... placeholders) {
        String message = messages.get(key);

        if (message != null) {
            message = String.format(colorize(message), placeholders);
        }

        return message;
    }

    private String colorize(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll(result -> {
            ChatColor color = Enums.getIfPresent(ChatColor.class, result.group(1).toUpperCase()).orNull();
            return color != null ? color.toString() : "";
        });
    }

    @Override
    public void reload(@Nullable Logger logger) {
        // TODO: If there are keys in the default messages.json, add them to the one saved on disk!

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                this.warning(logger, () -> "Can't read " + filePath.getFileName() + ". Expected JSON object but got " + root.getClass().getName());
                return;
            }

            JsonObject rootObject = root.getAsJsonObject();
            this.messages = new HashMap<>(rootObject.size());

            for (Entry<String, JsonElement> entry : rootObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (!value.isJsonPrimitive()) {
                    this.warning(logger, () -> "Message key " + key + " should be a string, but was a " + value.getClass().getName() + ". Ignoring");
                    continue;
                }

                String message = value.getAsString();
                this.messages.put(key, message);
            }
        } catch (IOException e) {
            this.warning(logger, () -> "Something went wrong while reading " + filePath.getFileName() + "! Reason: \"" + e.getMessage() + "\"");
        }
    }

    private void warning(Logger logger, Supplier<String> message) {
        if (logger == null) {
            return;
        }

        logger.warning(message);
    }

}
