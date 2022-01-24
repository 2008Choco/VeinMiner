package wtf.choco.veinminer.config;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeinMinerConfigTest {

    @Test
    void testIsDisabledWorld() {
        VeinMinerConfig config = VeinMinerConfig.builder()
                .disableWorld("world")
                .disableWorld("other_world")
                .build();

        assertTrue(config.isDisabledWorld("world"));
        assertTrue(config.isDisabledWorld("other_world"));
        assertFalse(config.isDisabledWorld("undisabled_world"));
    }

    @Test
    void testGetDisabledWorlds() {
        VeinMinerConfig config = VeinMinerConfig.builder()
                .disableWorld("world")
                .disableWorld("other_world")
                .build();

        assertEquals(Set.of("world", "other_world"), config.getDisabledWorlds());

        assertThrows(UnsupportedOperationException.class, () -> config.getDisabledWorlds().remove("world"), "getDisabledWorlds() must be immutable");
        assertThrows(UnsupportedOperationException.class, () -> config.getDisabledWorlds().add("new_world"), "getDisabledWorlds() must be immutable");
        assertThrows(UnsupportedOperationException.class, () -> config.getDisabledWorlds().clear(), "getDisabledWorlds() must be immutable");
    }

    @Test
    void testEdit() {
        VeinMinerConfig config = VeinMinerConfig.builder()
                .repairFriendly(true)
                .maxVeinSize(64)
                .cost(100.0)
                .disableWorld("world")
                .disableWorld("other_world")
                .build();

        VeinMinerConfig edited = config.edit(builder -> builder
                .repairFriendly(false)
                .maxVeinSize(32)
                .cost(50.0)
                .undisableWorld("other_world")
        );

        assertNotEquals(config, edited); // Also checks if not the same instance by definition of equals()
        assertAll(
            () -> assertFalse(edited.isRepairFriendly()),
            () -> assertEquals(32, edited.getMaxVeinSize()),
            () -> assertEquals(50.0, edited.getCost()),
            () -> assertEquals(Set.of("world"), edited.getDisabledWorlds())
        );
    }

    @Test
    void testClone() {
        VeinMinerConfig config = VeinMinerConfig.builder()
                .repairFriendly(true)
                .maxVeinSize(64)
                .cost(100.0)
                .disableWorld("world")
                .disableWorld("other_world")
                .build();

        assertEquals(config, config.clone());
    }

}
