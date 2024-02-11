package wtf.choco.veinminer.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void testCopyPartialMatches() {
        assertLinesMatch(List.of("hello world", "hello"), StringUtils.copyPartialMatches("hell", List.of("hello world", "hello", "hi"), new ArrayList<>()));
        assertLinesMatch(List.of("Professional", "pRops", "proDUCT"), StringUtils.copyPartialMatches("pro", List.of("pride", "Professional", "apple", "pRops", "proDUCT"), new ArrayList<>()));
    }

    @Test
    void testStartsWithIgnoreCase() {
        assertTrue(StringUtils.startsWithIgnoreCase("This is a string", "thIS Is"));
        assertFalse(StringUtils.startsWithIgnoreCase("This doesn't start with this prefix:", "doesn't"));
    }

    @Test
    void testRepeat() {
        assertEquals("----------", StringUtils.repeat('-', 10));
        assertEquals(".....", StringUtils.repeat('.', 5));
    }

    @Test
    void testToInt() {
        assertEquals(69, StringUtils.toInt("69"));
        assertEquals(-420, StringUtils.toInt("-420"));
        assertEquals(0, StringUtils.toInt("invalid", 0));
        assertEquals(-1, StringUtils.toInt("nonsense", -1));
    }

}
