package co.wethinkcode.healthsafe;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record EquipmentFailureEvent(
        String eventId,
        String wardId,
        String equipment,
        String details,
        String reportedAt) {

    public static EquipmentFailureEvent create(String wardId, String equipment, String details) {
        return new EquipmentFailureEvent(
                UUID.randomUUID().toString(),
                wardId.trim().toUpperCase(Locale.ROOT),
                equipment.trim(),
                details == null ? "" : details.trim(),
                Instant.now().toString());
    }
}
