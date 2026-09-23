package co.wethinkcode.healthsafe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class WardDirectory {

    private final List<WardRecord> wards;
    private final Map<String, WardRecord> wardsById;

    public WardDirectory(List<WardRecord> wards) {
        this.wards = List.copyOf(wards);
        this.wardsById = new LinkedHashMap<>();
        for (WardRecord ward : this.wards) {
            wardsById.put(normalizeId(ward.wardId()), ward);
        }
    }

    public List<WardRecord> wards() {
        return wards;
    }

    public Optional<WardRecord> find(String wardId) {
        return Optional.ofNullable(wardsById.get(normalizeId(wardId)));
    }

    public List<String> departments() {
        return wards.stream()
                .map(WardRecord::department)
                .filter(department -> department != null && !department.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    private String normalizeId(String wardId) {
        return wardId == null ? "" : wardId.trim().toUpperCase(Locale.ROOT);
    }
}
