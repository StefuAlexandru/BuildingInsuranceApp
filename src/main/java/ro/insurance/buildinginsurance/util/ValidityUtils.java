package ro.insurance.buildinginsurance.util;

import java.time.LocalDate;

public final class ValidityUtils {

    private ValidityUtils() {}

    public static boolean isActiveOn(boolean active, LocalDate effectiveFrom, LocalDate effectiveTo, LocalDate date) {
        if (!active) return false;
        if (date == null) return false;
        if (effectiveFrom != null && date.isBefore(effectiveFrom)) return false;
        return effectiveTo == null || !date.isAfter(effectiveTo);
    }
}

