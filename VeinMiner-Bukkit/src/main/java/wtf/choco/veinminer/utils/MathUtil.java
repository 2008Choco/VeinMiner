package wtf.choco.veinminer.utils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;

public final class MathUtil {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");

    private MathUtil() { }

    /**
     * Parse a timestamp value (i.e. 1w2d3h4m5s) and return its value in seconds.
     *
     * @param value the value to parse
     * @param defaultSeconds the value to return if "value" is null (i.e. from a config)
     *
     * @return the amount of time in seconds represented by the supplied value
     */
    public static int parseSeconds(String value, int defaultSeconds) {
        return (value != null) ? parseSeconds(value) : defaultSeconds;
    }

    /**
     * Parse a timestamp value (i.e. 1w2d3h4m5s) and return its value in seconds.
     *
     * @param value the value to parse
     *
     * @return the amount of time in seconds represented by the supplied value
     */
    public static int parseSeconds(String value) {
        // Handle legacy (i.e. no timestamps... for example, just "600")
        int legacyTime = NumberUtils.toInt(value, -1);
        if (legacyTime != -1) {
            return legacyTime;
        }

        int seconds = 0;

        Matcher matcher = TIME_PATTERN.matcher(value);
        while (matcher.find()) {
            int amount = NumberUtils.toInt(matcher.group(1));

            switch (matcher.group(2)) {
                case "w":
                    seconds += TimeUnit.DAYS.toSeconds(amount * 7);
                    break;
                case "d":
                    seconds += TimeUnit.DAYS.toSeconds(amount);
                    break;
                case "h":
                    seconds += TimeUnit.HOURS.toSeconds(amount);
                    break;
                case "m":
                    seconds += TimeUnit.MINUTES.toSeconds(amount);
                    break;
                case "s":
                    seconds += amount;
                    break;
            }
        }

        return seconds;
    }

}
