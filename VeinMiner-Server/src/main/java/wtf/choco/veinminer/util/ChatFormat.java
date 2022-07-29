package wtf.choco.veinminer.util;

import org.jetbrains.annotations.NotNull;

/**
 * All supported legacy chat colour and format codes.
 */
public enum ChatFormat {

    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    MAGIC('k', true),
    BOLD('l', true),
    STRIKETHROUGH('m', true),
    UNDERLINE('n', true),
    ITALIC('o', true),
    RESET('r');

    public static final char COLOR_CHAR = '\u00A7';

    private final boolean format;
    private final String toString;

    private ChatFormat(char code, boolean format) {
        this.format = format;
        this.toString = COLOR_CHAR + String.valueOf(code);
    }

    private ChatFormat(char code) {
        this(code, false);
    }

    @NotNull
    @Override
    public String toString() {
        return toString;
    }

    /**
     * Checks if this code is a format code as opposed to a color code.
     *
     * @return whether this ChatColor is a format code
     */
    public boolean isFormat() {
        return format;
    }

    /**
     * Checks if this code is a color code as opposed to a format code.
     *
     * @return whether this ChatColor is a color code
     */
    public boolean isColor() {
        return !format && this != RESET;
    }

    /**
     * Translates a string using an alternate color code character into a
     * string that uses the {@link #COLOR_CHAR color code character}. The
     * alternate color code character will only be replaced if it is
     * immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar the alternate color code character to replace. Ex: {@literal &}
     * @param string the string the translate
     *
     * @return text containing the {@link #COLOR_CHAR color code character}
     */
    @NotNull
    public static String translateAlternateColorCodes(char altColorChar, @NotNull String string) {
        char[] b = string.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = ChatFormat.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

}
