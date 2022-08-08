package wtf.choco.veinminer.util;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class BlockPositionTest {

    private static final int MAX_XZ = 30000000, MAX_Y = 320, MIN_Y = -64;

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

    @Test
    void testPack() {
        BlockPosition position = new BlockPosition(0, 0, 0);
        assertEquals(0, position.pack());
    }

    @Test
    void testPackUnpack() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(-MAX_XZ, MAX_XZ), y = random.nextInt(MIN_Y, MAX_Y), z = random.nextInt(-MAX_XZ, MAX_XZ);

        BlockPosition position = new BlockPosition(x, y, z);
        BlockPosition unpacked = BlockPosition.unpack(position.pack());

        assertEquals(position, unpacked);
    }

}
