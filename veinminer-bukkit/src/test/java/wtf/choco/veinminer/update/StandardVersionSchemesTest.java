package wtf.choco.veinminer.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static wtf.choco.veinminer.update.StandardVersionSchemes.DECIMAL;

class StandardVersionSchemesTest {

    @Test
    void testDecimal() {
        // Assert comparisons with a variety (but equivalent amount of) decimal counts
        assertEquals(1, DECIMAL.compareVersions("1.0.0", "0.0.0"));
        assertEquals(-1, DECIMAL.compareVersions("0.0.0", "1.0.0"));
        assertEquals(1, DECIMAL.compareVersions("0.1.0", "0.0.0"));
        assertEquals(-1, DECIMAL.compareVersions("0.0.0", "0.1.0"));
        assertEquals(1, DECIMAL.compareVersions("0.0.1", "0.0.0"));
        assertEquals(-1, DECIMAL.compareVersions("0.0.0", "0.0.1"));
        assertEquals(1, DECIMAL.compareVersions("1.0", "0.0"));
        assertEquals(-1, DECIMAL.compareVersions("0.0", "1.0"));
        assertEquals(1, DECIMAL.compareVersions("0.1", "0.0"));
        assertEquals(-1, DECIMAL.compareVersions("0.0", "0.1"));

        // Assert comparisons with a different amount of decimals
        assertEquals(1, DECIMAL.compareVersions("2.2.2", "2.2"));
        assertEquals(-1, DECIMAL.compareVersions("2.1.2", "2.2"));

        // Assert a few random number comparisons
        assertEquals(-1, DECIMAL.compareVersions("1.2.3", "1.3.2"));
        assertEquals(-1, DECIMAL.compareVersions("6.42.10.1", "6.42.10.4"));
        assertEquals(1, DECIMAL.compareVersions("3.2", "1.0"));

        // Assert version equality
        assertEquals(0, DECIMAL.compareVersions("0.0.5", "0.0.5"));
        assertEquals(0, DECIMAL.compareVersions("1.0.2.3", "1.0.2.3"));
        assertEquals(0, DECIMAL.compareVersions("15", "15"));

        assertThrows(UnsupportedOperationException.class, () -> DECIMAL.compareVersions("unsupported-format", "0.0.0"));
    }

}
