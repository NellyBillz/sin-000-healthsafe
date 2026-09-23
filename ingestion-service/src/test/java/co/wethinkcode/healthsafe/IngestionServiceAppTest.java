package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngestionServiceAppTest {

    @Test
    void loadsAndCleansTheBundledWardExport() throws Exception {
        List<WardRecord> wards = IngestionServiceApp.loadWards();

        assertEquals(17, wards.size());
        assertEquals("W-01", wards.get(0).wardId());
        assertTrue(wards.stream().anyMatch(ward ->
                ward.wardId().equals("W-05") && ward.bedsAvailable() == 5));
    }
}
