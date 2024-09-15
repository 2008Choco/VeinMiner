package wtf.choco.veinminer.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class holding constants representing standard version schemes.
 */
public final class StandardVersionSchemes {

    /**
     * x.x.x.x (repeating).
     */
    public static final VersionScheme DECIMAL = (first, second) -> {
        String[] firstSplit = splitVersionInfo(first), secondSplit = splitVersionInfo(second);

        int decimalCount = Math.min(firstSplit.length, secondSplit.length);
        for (int i = 0; i < decimalCount; i++) {
            int firstValue = toInt(firstSplit[i], -1), secondValue = toInt(secondSplit[i], -1);

            if (firstValue > secondValue) {
                return 1;
            } else if (firstValue < secondValue) {
                return -1;
            }
        }

        /*
         * At this point, decimals up until the max decimalCount all match, so we defer to decimal count...
         * If the strings contain the same amount of decimals, they must be the same
         * If the first string has more decimals than the second, it must be newer (e.g. 1.2.3.1 is newer than 1.2.3)
         * The inverse is also true.
         *
         * Integer#compare() against the split lengths accomplishes exactly this.
         */

        return Integer.compare(firstSplit.length, secondSplit.length);
    };

    private static final Pattern DECIMAL_SCHEME_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)*");

    private StandardVersionSchemes() { }

    private static String[] splitVersionInfo(String version) {
        Matcher matcher = DECIMAL_SCHEME_PATTERN.matcher(version);

        if (!matcher.find()) {
            throw new UnsupportedOperationException("Malformatted version string: \"" + version + "\"");
        }

        return matcher.group().split("\\.");
    }

    private static int toInt(String string, int defaultValue) {
        if (string == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
