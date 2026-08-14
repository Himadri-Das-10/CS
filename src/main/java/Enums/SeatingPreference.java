package Enums;

/**
 * Standardized representation of seating preferences matching PostgreSQL seating_pref_enum.
 */
public enum SeatingPreference {
    FRONT("Front"),
    BACK("Back"),
    EMPTY("EMPTY");

    private final String dbValue;

    SeatingPreference(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return this == EMPTY ? "No Preference" : dbValue;
    }

    /**
     * Null-safe parser that converts any raw string to a SeatingPreference enum.
     *
     * @param text the input string
     * @return matching SeatingPreference, or SeatingPreference.EMPTY if null/unrecognized
     */
    public static SeatingPreference fromString(String text) {
        if (text == null || text.isBlank() || text.equalsIgnoreCase("Select")) {
            return EMPTY;
        }
        String clean = text.strip();
        for (SeatingPreference pref : values()) {
            if (pref.dbValue.equalsIgnoreCase(clean) || pref.name().equalsIgnoreCase(clean)) {
                return pref;
            }
        }
        return EMPTY;
    }

    /**
     * Checks whether the given text is a valid database enum value.
     */
    public static boolean isValid(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String clean = text.strip();
        for (SeatingPreference pref : values()) {
            if (pref.dbValue.equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }
}
