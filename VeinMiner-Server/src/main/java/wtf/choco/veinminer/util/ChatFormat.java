package wtf.choco.veinminer.util;

import org.jetbrains.annotations.NotNull;

/**
 * All supported legacy chat colour and format codes.
 */
public enum ChatFormat {

    /**
     * Black ({@literal &}0).
     */
    BLACK('0'),
    /**
     * Dark blue ({@literal &}1).
     */
    DARK_BLUE('1'),
    /**
     * Dark green ({@literal &}2).
     */
    DARK_GREEN('2'),
    /**
     * Dark aqua ({@literal &}3).
     */
    DARK_AQUA('3'),
    /**
     * Dark red ({@literal &}4).
     */
    DARK_RED('4'),
    /**
     * Dark purple ({@literal &}5).
     */
    DARK_PURPLE('5'),
    /**
     * Gold ({@literal &}6).
     */
    GOLD('6'),
    /**
     * Gray ({@literal &}7).
     */
    GRAY('7'),
    /**
     * Dark gray ({@literal &}8).
     */
    DARK_GRAY('8'),
    /**
     * Blue ({@literal &}9).
     */
    BLUE('9'),
    /**
     * Green ({@literal &}a).
     */
    GREEN('a'),
    /**
     * Aqua ({@literal &}b).
     */
    AQUA('b'),
    /**
     * Red ({@literal &}c).
     */
    RED('c'),
    /**
     * Light purple ({@literal &}d).
     */
    LIGHT_PURPLE('d'),
    /**
     * Yellow ({@literal &}e).
     */
    YELLOW('e'),
    /**
     * White ({@literal &}f).
     */
    WHITE('f'),
    /**
     * Magic/obfuscated text ({@literal &}k).
     */
    MAGIC('k', true),
    /**
     * Bold text ({@literal &}l).
     */
    BOLD('l', true),
    /**
     * Strikethrough text ({@literal &}m).
     */
    STRIKETHROUGH('m', true),
    /**
     * Underline text ({@literal &}n).
     */
    UNDERLINE('n', true),
    /**
     * Italic text ({@literal &}o).
     */
    ITALIC('o', true),
    /**
     * Reset formatting ({@literal &}r).
     */
    RESET('r');

    /**
     * The legacy colour character.
     */
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
