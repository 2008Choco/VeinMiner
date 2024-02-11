package wtf.choco.veinminer.util;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumUtilTest {

    @Test
    void testSuccess() {
        assertAll(
            () -> assertTrue(EnumUtil.get(ChatColor.class, "RED").isPresent()),
            () -> assertTrue(EnumUtil.get(ChatColor.class, "BLURPLE").isEmpty()),
            () -> assertSame(ChatColor.RED, EnumUtil.get(ChatColor.class, "RED").get())
        );
    }

}
