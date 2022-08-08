package wtf.choco.veinminer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamespacedKeyTest {

    @Test
    void testConstructor() {
        assertEquals("namespace:key", new NamespacedKey("namespace", "key").toString());
    }

    @Test
    void testMinecraft() {
        assertEquals("minecraft:example", NamespacedKey.minecraft("example").toString());
    }

    @Test
    void testVeinminer() {
        assertEquals("veinminer:example", NamespacedKey.veinminer("example").toString());
    }

    @Test
    void testEquals() {
        assertEquals(NamespacedKey.minecraft("example"), NamespacedKey.minecraft("example"));
    }

}
