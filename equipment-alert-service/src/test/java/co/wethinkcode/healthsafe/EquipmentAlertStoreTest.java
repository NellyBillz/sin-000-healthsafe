package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentAlertStoreTest {

    @Test
    void ignoresAnEventThatHasAlreadyBeenRecorded() {
        EquipmentAlertStore store = new EquipmentAlertStore();
        EquipmentFailureEvent event = new EquipmentFailureEvent(
                "event-1", "WARD-1", "Ventilator", "No power", "2026-09-23T00:00:00Z");

        assertTrue(store.record(event));
        assertFalse(store.record(event));
        assertEquals(1, store.alerts().size());
    }
}
