package co.wethinkcode.healthsafe;

import java.util.List;

public record WardRecord(
        String wardId,
        String wing,
        String department,
        Integer bedsAvailable,
        List<String> notes) {

    public WardRecord {
        notes = notes == null ? List.of() : List.copyOf(notes);
    }
}
