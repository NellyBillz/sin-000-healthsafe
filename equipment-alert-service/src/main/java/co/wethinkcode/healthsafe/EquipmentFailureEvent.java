package co.wethinkcode.healthsafe;

public record EquipmentFailureEvent(
        String eventId,
        String wardId,
        String equipment,
        String details,
        String reportedAt) {
}
