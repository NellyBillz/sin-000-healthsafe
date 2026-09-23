package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffingPlannerTest {

    private final StaffingPlanner planner = new StaffingPlanner();

    @Test
    void increasesRequiredStaffAsTheAlertLevelRises() {
        List<String> doctors = List.of("Dr A", "Dr B", "Dr C", "Dr D");

        assertEquals(1, planner.plan(doctors, 0).requiredDoctors());
        assertEquals(2, planner.plan(doctors, 3).requiredDoctors());
        assertEquals(3, planner.plan(doctors, 6).requiredDoctors());
        assertEquals(4, planner.plan(doctors, 8).requiredDoctors());
    }

    @Test
    void flagsAPlanWhenTooFewDoctorsAreAvailable() {
        StaffingPlanner.Plan plan = planner.plan(List.of("Dr A"), 8);

        assertEquals(List.of("Dr A"), plan.assignedDoctors());
        assertTrue(plan.understaffed());
    }

    @Test
    void removesBlankAndDuplicateDoctorNames() {
        StaffingPlanner.Plan plan = planner.plan(
                List.of("Dr A", " ", "Dr A", "Dr B"), 3);

        assertEquals(List.of("Dr A", "Dr B"), plan.assignedDoctors());
        assertFalse(plan.understaffed());
    }

    @Test
    void rejectsAnInvalidAlertLevel() {
        assertThrows(IllegalArgumentException.class,
                () -> planner.plan(List.of("Dr A"), 9));
    }
}
