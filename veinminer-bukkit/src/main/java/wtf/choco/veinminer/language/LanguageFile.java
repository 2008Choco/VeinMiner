package wtf.choco.veinminer.language;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a file-backed language definition.
 */
public interface LanguageFile {

    /**
     * Get the file path at which the language file is located.
     *
     * @return the file path
     */
    public Path getFilePath();

    /**
     * Get the message with the given key.
     *
     * @param key the message key
     *
     * @return the message, or the key if no message is configured
     */
    public String get(String key);

    /**
     * Get the message with the given key and apply placeholders (for a formatted string).
     *
     * @param key the message key
     * @param placeholders the placeholder values
     *
     * @return the formatted message, or the key if no message is configured
     */
    public String get(String key, Object... placeholders);

    /**
     * Send a configured message to a {@link CommandSender}.
     *
     * @param recipient the recipient to which the message should be sent
     * @param key the message key
     */
    public default void send(CommandSender recipient, String key) {
        recipient.sendMessage(get(key));
    }

    /**
     * Send a configured message with the given placeholders (for a formatted string) to a
     * {@link CommandSender}.
     *
     * @param recipient the recipient to which the message should be sent
     * @param key the message key
     * @param placeholders the placeholder values
     */
    public default void send(CommandSender recipient, String key, Object... placeholders) {
        recipient.sendMessage(get(key, placeholders));
    }

    /**
     * Reload this language file from disk into memory.
     *
     * @param logger the logger to use if something fails, or null to fail silently
     */
    public void reload(@Nullable Logger logger);

    /**
     * Reload this language file from disk into memory and fail silently.
     */
    public default void reload() {
        this.reload(null);
    }

}
