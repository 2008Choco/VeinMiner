package wtf.choco.veinminer.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageDirectionTest {

    @Test
    void testIsClientbound() {
        assertTrue(MessageDirection.CLIENTBOUND.isClientbound());

        // No other directions should be true for isClientbound()
        for (MessageDirection direction : MessageDirection.values()) {
            if (direction == MessageDirection.CLIENTBOUND) {
                continue;
            }

            assertFalse(direction.isClientbound());
        }
    }

    @Test
    void testIsServerbound() {
        assertTrue(MessageDirection.SERVERBOUND.isServerbound());

        // No other directions should be true for isServerbound()
        for (MessageDirection direction : MessageDirection.values()) {
            if (direction == MessageDirection.SERVERBOUND) {
                continue;
            }

            assertFalse(direction.isServerbound());
        }
    }

}
