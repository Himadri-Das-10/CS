package Enums;

/**
 * Central utility class for null-safe enum conversions and formatting
 * between the UI layer, Domain model, and PostgreSQL database layer.
 */
public final class EnumMapper {

    private EnumMapper() {
        // Utility class: prevent instantiation
    }

    /**
     * Converts any raw string to a database-compliant ClassLevel string.
     */
    public static String toClassLevelDb(String text) {
        return ClassLevel.fromString(text).getDbValue();
    }

    /**
     * Converts any raw string to a database-compliant Division string.
     */
    public static String toDivisionDb(String text) {
        return Division.fromString(text).getDbValue();
    }

    /**
     * Converts any raw string to a database-compliant Sex string ('Male', 'Female', 'EMPTY').
     */
    public static String toSexDb(String text) {
        return Sex.fromString(text).getDbValue();
    }

    /**
     * Converts any raw string to a database-compliant SeatingPreference string ('Front', 'Back', 'EMPTY').
     */
    public static String toSeatingPrefDb(String text) {
        return SeatingPreference.fromString(text).getDbValue();
    }

    /**
     * Formats database values ("EMPTY", "-1", null) into clean user-facing strings ("Unspecified").
     */
    public static String formatForDisplay(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("EMPTY") || value.equals("-1")) {
            return "Unspecified";
        }
        return value.strip();
    }
}
