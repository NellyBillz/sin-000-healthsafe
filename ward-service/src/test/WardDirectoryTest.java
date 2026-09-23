package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WardDirectoryTest {

    private final WardDirectory directory = new WardDirectory(List.of(
            new WardRecord("W-01", "East Wing", "Cardiology", 3, List.of()),
            new WardRecord("W-02", "West Wing", "Paediatrics", null, List.of()),
            new WardRecord("W-03", "East Wing", "Cardiology", 0, List.of())
    ));

    @Test
    void findsAWardWithoutDependingOnIdCasing() {
        assertTrue(directory.find(" w-01 ").isPresent());
        assertEquals("Cardiology", directory.find("w-01").orElseThrow().department());
    }

    @Test
    void returnsAnEmptyResultForAnUnknownWard() {
        assertTrue(directory.find("W-99").isEmpty());
    }

    @Test
    void listsUniqueDepartmentsInAlphabeticalOrder() {
        assertEquals(List.of("Cardiology", "Paediatrics"), directory.departments());
    }
}
