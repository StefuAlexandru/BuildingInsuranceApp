package ro.insurance.buildinginsurance.util;

import java.time.LocalDate;

public final class ValidityUtils {

    private ValidityUtils() {}

    public static boolean isActiveOn(LocalDate effectiveFrom, LocalDate effectiveTo, LocalDate date) {
        if (date == null) return false;
        if (effectiveFrom != null && date.isBefore(effectiveFrom)) return false;
        return effectiveTo == null || !date.isAfter(effectiveTo);
    }


}

