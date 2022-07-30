package wtf.choco.veinminer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumUtilTest {

    @Test
    void testSuccess() {
        assertAll(
            () -> assertTrue(EnumUtil.get(ChatFormat.class, "RED").isPresent()),
            () -> assertTrue(EnumUtil.get(ChatFormat.class, "BLURPLE").isEmpty()),
            () -> assertSame(ChatFormat.RED, EnumUtil.get(ChatFormat.class, "RED").get())
        );
    }

}
