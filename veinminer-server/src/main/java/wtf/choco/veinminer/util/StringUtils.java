package wtf.choco.veinminer.util;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

/**
 * Various utility methods handy for tab completion.
 */
public final class StringUtils {

    private StringUtils() { }

    /**
     * Copies all elements from the iterable collection of originals to the
     * collection provided.
     *
     * @param <T> the collection of strings
     * @param token string to search for
     * @param originals an iterable collection of strings to filter.
     * @param collection the collection to add matches to
     * @return the collection provided that would have the elements copied into
     *
     * @throws UnsupportedOperationException if the collection is immutable
     * and originals contains a string which starts with the specified
     * search string
     * @throws IllegalArgumentException if originals contains a null element
     */
    @NotNull
    public static <T extends Collection<? super String>> T copyPartialMatches(@NotNull String token, @NotNull Iterable<String> originals, @NotNull T collection) {
        for (String string : originals) {
            if (startsWithIgnoreCase(string, token)) {
                collection.add(string);
            }
        }

        return collection;
    }

    /**
     * This method uses a region to check case-insensitive equality. This
     * means the internal array does not need to be copied like a
     * toLowerCase() call would.
     *
     * @param string the string to check
     * @param prefix prefix of string to compare
     *
     * @return true if provided string starts with, ignoring case, the prefix provided
     */
    public static boolean startsWithIgnoreCase(@NotNull String string, @NotNull String prefix) {
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Repeat the given character x amount of times and buffer it to a string.
     *
     * @param character the character to repeat
     * @param times the amount of times to repeat the character
     *
     * @return a string containing the repeated characters
     */
    @NotNull
    public static String repeat(char character, int times) {
        if (times <= 0) {
            throw new IllegalArgumentException("Cannot repeat less than 1 time");
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < times; i++) {
            builder.append(character);
        }

        return builder.toString();
    }

    /**
     * Convert the given string to an integer primitive, or a default value if the
     * string is not in a supported number format.
     *
     * @param string the string to parse
     * @param defaultValue the default value to return if the parsing fails
     *
     * @return the int representation of the string, or the default value
     */
    public static int toInt(@NotNull String string, int defaultValue) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert the given string to an integer primitive, or 0 if the string is not in
     * a supported number format.
     *
     * @param string the string to parse
     *
     * @return the int representation of the string, or 0
     */
    public static int toInt(@NotNull String string) {
        return toInt(string, 0);
    }

}
