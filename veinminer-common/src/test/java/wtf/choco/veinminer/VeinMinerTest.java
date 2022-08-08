package wtf.choco.veinminer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VeinMinerTest {

    @Test
    void testChannel() {
        assertEquals(VeinMiner.PROTOCOL.getVersion(), VeinMiner.PROTOCOL_VERSION);
    }

    @Test
    void testVersion() {
        assertEquals(VeinMiner.PROTOCOL.getChannel(), VeinMiner.PROTOCOL_CHANNEL);
    }

    @Test
    void testBlockStatePattern() {
        // Even with states that don't exist, the pattern should still match
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest[waterlogged=true,facing=north,random_state=value_that_doesnt_exist]").matches());

        // Test all other valid cases (with minecraft: namespace)
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest[waterlogged=true,facing=north]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest[facing=north,waterlogged=true]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest[waterlogged=true]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest[facing=north]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest[]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft:chest").matches());

        // Test all other valid cases (without minecraft: namespace)
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("chest[waterlogged=true,facing=north]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("chest[facing=north,waterlogged=true]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("chest[waterlogged=true]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("chest[facing=north]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("chest[]").matches());
        assertTrue(VeinMiner.PATTERN_BLOCK_STATE.matcher("chest").matches());

        // Test invalid cases
        assertFalse(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft::chest[waterlogged=true,facing=north]").matches());
        assertFalse(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft::chest").matches());
        assertFalse(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft::chest[waterlogged=,]").matches());
        assertFalse(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft::chest][").matches());
        assertFalse(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft::chest[").matches());
        assertFalse(VeinMiner.PATTERN_BLOCK_STATE.matcher("minecraft::chest]").matches());
    }

}
