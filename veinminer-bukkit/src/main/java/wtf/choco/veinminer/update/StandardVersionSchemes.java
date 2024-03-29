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

        for (int i = 0; i < Math.min(firstSplit.length, secondSplit.length); i++) {
            int currentValue = toInt(firstSplit[i], -1), newestValue = toInt(secondSplit[i], -1);

            if (newestValue > currentValue) {
                return second;
            } else if (newestValue < currentValue) {
                return first;
            }
        }

        return (secondSplit.length > firstSplit.length) ? second : first;
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
