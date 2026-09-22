package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WardCsvCleanerTest {

    private final WardCsvCleaner cleaner = new WardCsvCleaner();

    @Test
    void normalizesNamesIdentifiersWhitespaceAndMissingValues() throws Exception {
        String csv = "ward_id,Wing,department,beds_available\n"
                + " w-02 ,South  wing,PAEDIATRICS,N/A\n";

        WardRecord ward = cleaner.clean(new StringReader(csv)).get(0);

        assertEquals("W-02", ward.wardId());
        assertEquals("South Wing", ward.wing());
        assertEquals("Paediatrics", ward.department());
        assertNull(ward.bedsAvailable());
        assertTrue(ward.notes().get(0).contains("missing"));
    }

    @Test
    void flagsInvalidBedCountsWithoutCrashing() throws Exception {
        String csv = "ward_id,Wing,department,beds_available\n"
                + "W-01,East Wing,Cardiology,five\n"
                + "W-02,West Wing,Oncology,-1\n";

        List<WardRecord> wards = cleaner.clean(new StringReader(csv));

        assertEquals(2, wards.size());
        assertNull(wards.get(0).bedsAvailable());
        assertNull(wards.get(1).bedsAvailable());
        assertTrue(wards.get(0).notes().get(0).contains("non-numeric"));
        assertTrue(wards.get(1).notes().get(0).contains("range"));
    }

    @Test
    void mergesDuplicateWardIdsAndKeepsTheFirstValidValue() throws Exception {
        String csv = "ward_id,Wing,department,beds_available\n"
                + "W-05,East Wing,Paediatrics,5\n"
                + "w-05,east wing,PAEDIATRICS,five\n";

        List<WardRecord> wards = cleaner.clean(new StringReader(csv));

        assertEquals(1, wards.size());
        assertEquals(5, wards.get(0).bedsAvailable());
        assertTrue(wards.get(0).notes().stream().anyMatch(note -> note.contains("Duplicate")));
    }
}
