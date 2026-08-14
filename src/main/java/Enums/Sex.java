package Enums;

/**
 * Standardized representation of student gender matching PostgreSQL sex_enum.
 */
public enum Sex {
    MALE("Male"),
    FEMALE("Female"),
    EMPTY("EMPTY");

    private final String dbValue;

    Sex(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return this == EMPTY ? "Unspecified" : dbValue;
    }

    /**
     * Null-safe parser that converts any raw string to a Sex enum.
     * Maps 'Prefer Not To Say', 'Choose', null, and empty strings cleanly to Sex.EMPTY.
     *
     * @param text the input string
     * @return matching Sex, or Sex.EMPTY if null/unrecognized
     */
    public static Sex fromString(String text) {
        if (text == null || text.isBlank()) {
            return EMPTY;
        }
        String clean = text.strip();
        if (clean.equalsIgnoreCase("Prefer Not To Say") || clean.equalsIgnoreCase("Choose") || clean.equalsIgnoreCase("EMPTY")) {
            return EMPTY;
        }
        for (Sex sex : values()) {
            if (sex.dbValue.equalsIgnoreCase(clean) || sex.name().equalsIgnoreCase(clean)) {
                return sex;
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
        for (Sex sex : values()) {
            if (sex.dbValue.equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }
}
