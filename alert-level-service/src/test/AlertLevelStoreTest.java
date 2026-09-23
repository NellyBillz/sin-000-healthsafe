package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertLevelStoreTest {

    @Test
    void startsAtLevelZero() {
        assertEquals(0, new AlertLevelStore().get());
    }

    @Test
    void acceptsEveryLevelFromZeroThroughEight() {
        AlertLevelStore store = new AlertLevelStore();

        for (int level = 0; level <= 8; level++) {
            store.set(level);
            assertEquals(level, store.get());
        }
    }

    @Test
    void rejectsLevelsOutsideTheAllowedRange() {
        AlertLevelStore store = new AlertLevelStore();

        assertThrows(IllegalArgumentException.class, () -> store.set(-1));
        assertThrows(IllegalArgumentException.class, () -> store.set(9));
        assertEquals(0, store.get());
    }
}
