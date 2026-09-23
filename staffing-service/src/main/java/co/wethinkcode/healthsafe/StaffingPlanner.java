package co.wethinkcode.healthsafe;

import java.util.List;

public final class StaffingPlanner {

    public Plan plan(List<String> availableDoctors, int alertLevel) {
        if (alertLevel < 0 || alertLevel > 8) {
            throw new IllegalArgumentException("Alert level must be between 0 and 8");
        }

        List<String> doctors = availableDoctors.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        int requiredDoctors = requiredDoctors(alertLevel);
        List<String> assignedDoctors = doctors.stream()
                .limit(requiredDoctors)
                .toList();

        return new Plan(
                requiredDoctors,
                assignedDoctors,
                assignedDoctors.size() < requiredDoctors);
    }

    private int requiredDoctors(int alertLevel) {
        if (alertLevel <= 2) {
            return 1;
        }
        if (alertLevel <= 5) {
            return 2;
        }
        if (alertLevel <= 7) {
            return 3;
        }
        return 4;
    }

    public record Plan(
            int requiredDoctors,
            List<String> assignedDoctors,
            boolean understaffed) {
    }
}
