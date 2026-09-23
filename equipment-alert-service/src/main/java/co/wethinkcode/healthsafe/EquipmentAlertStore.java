package co.wethinkcode.healthsafe;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EquipmentAlertStore {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private final List<EquipmentFailureEvent> alerts = new CopyOnWriteArrayList<>();

    public boolean record(EquipmentFailureEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()) {
            return false;
        }
        if (!processedEventIds.add(event.eventId())) {
            return false;
        }
        alerts.add(event);
        return true;
    }

    public List<EquipmentFailureEvent> alerts() {
        return List.copyOf(alerts);
    }
}
