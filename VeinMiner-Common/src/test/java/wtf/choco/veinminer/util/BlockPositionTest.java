package wtf.choco.veinminer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class BlockPositionTest {

    @Test
    void testOffset() {
        BlockPosition position = new BlockPosition(10, 10, 10);

        // Should be equals(), but not ==
        BlockPosition zero = position.offset(0, 0, 0);
        assertEquals(position, zero);
        assertNotSame(position, zero);

        BlockPosition offsetPositive = position.offset(10, 10, 10);
        assertAll(
            () -> assertEquals(20, offsetPositive.x()),
            () -> assertEquals(20, offsetPositive.y()),
            () -> assertEquals(20, offsetPositive.z())
        );

        BlockPosition offsetNegative = position.offset(-10, -10, -10);
        assertAll(
            () -> assertEquals(0, offsetNegative.x()),
            () -> assertEquals(0, offsetNegative.y()),
            () -> assertEquals(0, offsetNegative.z())
        );

        // Double check that a zero'd BlockPosition and the zero obtained from an offset are equals()
        assertEquals(new BlockPosition(0, 0, 0), offsetNegative);
    }

}
