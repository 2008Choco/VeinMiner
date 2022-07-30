package wtf.choco.veinminer.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static wtf.choco.veinminer.update.StandardVersionSchemes.DECIMAL;

class StandardVersionSchemesTest {

    @Test
    void testDecimal() {
        // Verify both that this works and that order does not matter
        assertAll(
            () -> assertEquals("1.0.0", DECIMAL.compareVersions("1.0.0", "0.0.0")),
            () -> assertEquals("1.0.0", DECIMAL.compareVersions("0.0.0", "1.0.0")),
            () -> assertEquals("0.1.0", DECIMAL.compareVersions("0.1.0", "0.0.0")),
            () -> assertEquals("0.1.0", DECIMAL.compareVersions("0.0.0", "0.1.0")),
            () -> assertEquals("0.0.1", DECIMAL.compareVersions("0.0.1", "0.0.0")),
            () -> assertEquals("0.0.1", DECIMAL.compareVersions("0.0.0", "0.0.1"))
        );

        // Verify that this works with only two decimals and that order does not matter
        assertAll(
            () -> assertEquals("1.0", DECIMAL.compareVersions("1.0", "0.0")),
            () -> assertEquals("1.0", DECIMAL.compareVersions("0.0", "1.0")),
            () -> assertEquals("0.1", DECIMAL.compareVersions("0.1", "0.0")),
            () -> assertEquals("0.1", DECIMAL.compareVersions("0.0", "0.1"))
        );

        // Verify comparing versions with varying degrees of numbers
        assertAll(
            () -> assertEquals("2.2.2", DECIMAL.compareVersions("2.2.2", "2.2")),
            () -> assertEquals("2.2", DECIMAL.compareVersions("2.1.2", "2.2"))
        );

        // Verify a few random numbers
        assertAll(
            () -> assertEquals("1.3.2", DECIMAL.compareVersions("1.2.3", "1.3.2")),
            () -> assertEquals("6.42.10.4", DECIMAL.compareVersions("6.42.10.1", "6.42.10.4")),
            () -> assertEquals("3.2", DECIMAL.compareVersions("3.2", "1.0"))
        );

        assertThrows(UnsupportedOperationException.class, () -> DECIMAL.compareVersions("unsupported-format", "0.0.0"));
    }

}
