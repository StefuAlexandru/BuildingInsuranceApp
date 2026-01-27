package ro.insurance.buildinginsurance.util;

public final class StringNormalizer {

    private StringNormalizer() {}

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    public static String normalizeUpper(String s) {
        String t = trimToNull(s);
        return t == null ? null : t.toUpperCase();
    }
}
