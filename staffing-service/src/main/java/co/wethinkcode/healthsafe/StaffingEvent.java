package co.wethinkcode.healthsafe;

import java.util.List;

public record StaffingEvent(
        String wardId,
        int alertLevel,
        int requiredDoctors,
        List<String> assignedDoctors,
        boolean understaffed) {

    public StaffingEvent {
        assignedDoctors = List.copyOf(assignedDoctors);
    }
}
