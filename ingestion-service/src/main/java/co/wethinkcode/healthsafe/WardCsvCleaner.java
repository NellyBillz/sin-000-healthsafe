package co.wethinkcode.healthsafe;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class WardCsvCleaner {

    private static final Set<String> MISSING_VALUES = Set.of(
            "", "n/a", "tbd", "unknown", "-", "nan");

    public List<WardRecord> clean(Reader source) throws IOException {
        try (CSVReader reader = new CSVReader(source)) {
            List<String[]> rows = reader.readAll();
            Map<String, WardRecord> wardsById = new LinkedHashMap<>();

            for (int index = 1; index < rows.size(); index++) {
                WardRecord ward = cleanRow(rows.get(index), index + 1);
                if (ward != null) {
                    wardsById.merge(ward.wardId(), ward, this::mergeDuplicate);
                }
            }

            return List.copyOf(wardsById.values());
        } catch (CsvException exception) {
            throw new IOException("Unable to parse ward CSV", exception);
        }
    }

    private WardRecord cleanRow(String[] row, int rowNumber) {
        if (row.length < 4) {
            return null;
        }

        String wardId = normalizeWardId(row[0]);
        if (wardId == null) {
            return null;
        }

        List<String> notes = new ArrayList<>();
        return new WardRecord(
                wardId,
                normalizeName(row[1]),
                normalizeDepartment(row[2]),
                normalizeBeds(row[3], rowNumber, notes),
                notes);
    }

    private WardRecord mergeDuplicate(WardRecord first, WardRecord duplicate) {
        List<String> notes = new ArrayList<>(first.notes());
        notes.addAll(duplicate.notes());
        notes.add("Duplicate record merged for " + first.wardId());

        return new WardRecord(
                first.wardId(),
                preferPresent(first.wing(), duplicate.wing()),
                preferPresent(first.department(), duplicate.department()),
                first.bedsAvailable() != null ? first.bedsAvailable() : duplicate.bedsAvailable(),
                notes);
    }

    private String normalizeWardId(String value) {
        String normalized = normalizeWhitespace(value).toUpperCase(Locale.ROOT);
        return isMissing(normalized) ? null : normalized;
    }

    private String normalizeDepartment(String value) {
        String normalized = normalizeName(value);
        if ("Pediatrics".equals(normalized)) {
            return "Paediatrics";
        }
        if ("Icu".equals(normalized)) {
            return "ICU";
        }
        return normalized;
    }

    private String normalizeName(String value) {
        String normalized = normalizeWhitespace(value).toLowerCase(Locale.ROOT);
        if (isMissing(normalized)) {
            return null;
        }

        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalize = true;
        for (char character : normalized.toCharArray()) {
            result.append(capitalize ? Character.toUpperCase(character) : character);
            capitalize = Character.isWhitespace(character);
        }
        return result.toString();
    }

    private Integer normalizeBeds(String value, int rowNumber, List<String> notes) {
        String normalized = normalizeWhitespace(value).toLowerCase(Locale.ROOT);
        if (isMissing(normalized)) {
            notes.add("bedsAvailable was missing at CSV row " + rowNumber);
            return null;
        }

        try {
            int beds = Integer.parseInt(normalized);
            if (beds < 0 || beds > 1000) {
                notes.add("bedsAvailable was outside the accepted range at CSV row " + rowNumber);
                return null;
            }
            return beds;
        } catch (NumberFormatException exception) {
            notes.add("bedsAvailable was non-numeric at CSV row " + rowNumber);
            return null;
        }
    }

    private String preferPresent(String first, String second) {
        return first != null ? first : second;
    }

    private boolean isMissing(String value) {
        return MISSING_VALUES.contains(value.toLowerCase(Locale.ROOT));
    }

    private String normalizeWhitespace(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
