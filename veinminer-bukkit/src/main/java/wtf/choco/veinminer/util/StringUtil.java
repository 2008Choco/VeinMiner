package wtf.choco.veinminer.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of string utility methods.
 */
public final class StringUtil {

    private StringUtil() { }

    /**
     * Substring the provided {@code input} string starting at the index of the provided character, offset
     * by the amount of times the character occurs, excluding the character itself. For example:
     * <pre>
     * substringFromOccurrence("hello_world_how_are_you", '_', 0); // "world_how_are_you"
     * substringFromOccurrence("hello_world_how_are_you", '_', 1); // "how_are_you"
     * substringFromOccurrence("hello_world_how_are_you", '_', 2); // "are_you"
     * substringFromOccurrence("hello_world_how_are_you", '_', 3); // "you"
     * substringFromOccurrence("hello_world_how_are_you", '_', 4); // null
     * </pre>
     *
     * @param input the input string
     * @param character the character to look for
     * @param instanceOffset the instance of the character at which to start the substring
     *
     * @return the substring, or null if there were insufficient instances of the character in the string
     */
    @Nullable
    public static String substringFromOccurrence(@NotNull String input, char character, int instanceOffset) {
        int found = 0;

        int lastFoundCharAtIndex = -1;
        do {
            lastFoundCharAtIndex = input.indexOf(character, lastFoundCharAtIndex + 1);
        } while (++found <= instanceOffset);

        return lastFoundCharAtIndex < (input.length() - 1) ? input.substring(lastFoundCharAtIndex + 1) : null;
    }

}
