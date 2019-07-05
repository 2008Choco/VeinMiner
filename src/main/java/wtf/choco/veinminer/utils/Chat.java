package wtf.choco.veinminer.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

/**
 * Miscellaneous chat utilities
 *
 * @author Parker Hawke - Choco
 */
public final class Chat {

    private Chat() { }

    /**
     * Format a String and translate any keyed colour codes prefixed with the given char.
     * <p>
     * Every unique character prefixed with the given char will be considered a key assigned
     * to the colour provided by the {@code colours} parameter in sequential order. See below
     * for examples:
     *
     * <pre>
     *   translate('%', "%rThis is red and %athis is aqua, but it's now %r%bred and bold", ChatColor.RED, ChatColor.AQUA, ChatColor.BOLD);
     *   // Result: "(red)This is red and (aqua)this is aqua, but now it's (bold red)red and bold"
     * </pre>
     *
     * In the above example, {@code %r}, {@code %a}, and {@code %b} were used as keys.
     * Because {@code %r} was the first key, the first ChatColor, RED, is assigned to it.
     * Any recurring instance of this key will be translated to red. {@code a} was the
     * second key in the sequence, therefore the second ChatColor, AQUA, was assigned.
     * The same applies to bold which is the 3rd unique key.
     *
     * @param prefix the prefix identifier for colour translation keys
     * @param text the text to translate
     * @param colours the colours. Must have at least x amount of colours where x is the
     * amount of unique keys. If less, an UnsupportedOperationException will be thrown.
     *
     * @return the translated string
     */
    public static String translate(char prefix, String text, ChatColor... colours) {
        Map<Character, ChatColor> colourKey = new HashMap<>(colours.length);
        StringBuilder translated = new StringBuilder(text.length());

        int currentColourIndex = 0;
        char[] characters = text.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            char character = characters[i];

            if (character != prefix || i >= (characters.length - 1)) {
                translated.append(character);
                continue;
            } else if (i > 0 && character == prefix && characters[i - 1] == '\\') {
                translated.setCharAt(i - 1, prefix);
                continue;
            }

            char key = characters[i + 1];
            ChatColor colour = colourKey.get(key);
            if (colour == null) {
                if (currentColourIndex >= colours.length) {
                    throw new UnsupportedOperationException("Insufficient amount of colours for the provided keys.");
                }

                colour = colours[currentColourIndex++];
                colourKey.put(key, colour);
            }

            translated.append(colour.toString());
            i++; // Skip ahead an index to avoid the key, which should be ignored because it was considered here
        }

        translated.trimToSize();
        return translated.toString();
    }

    /**
     * Format a String and translate any keyed colour codes prefixed by {@code %}.
     * <p>
     * Every unique character prefixed with a {@code %} will be considered a key assigned
     * to the colour provided by the {@code colours} parameter in sequential order. See below
     * for examples:
     *
     * <pre>
     *   translate('%', "%rThis is red and %athis is aqua, but it's now %r%bred and bold", ChatColor.RED, ChatColor.AQUA, ChatColor.BOLD);
     *   // Result: "(red)This is red and (aqua)this is aqua, but now it's (bold red)red and bold"
     * </pre>
     *
     * In the above example, {@code %r}, {@code %a}, and {@code %b} were used as keys.
     * Because {@code %r} was the first key, the first ChatColor, RED, is assigned to it.
     * Any recurring instance of this key will be translated to red. {@code a} was the
     * second key in the sequence, therefore the second ChatColor, AQUA, was assigned.
     * The same applies to bold which is the 3rd unique key.
     *
     * @param text the text to translate
     * @param colours the colours. Must have at least x amount of colours where x is the
     * amount of unique keys. If less, an UnsupportedOperationException will be thrown.
     *
     * @return the translated string
     */
    public static String translate(String text, ChatColor... colours) {
        return translate('%', text, colours);
    }

}
