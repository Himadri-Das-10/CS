package Enums;

/**
 * Standardized representation of academic class levels matching PostgreSQL class_level_enum.
 */
public enum ClassLevel {
    PRIMARY_SCHOOL("Primary School"),
    MIDDLE_SCHOOL("Middle School"),
    SECONDARY_SCHOOL("Secondary School"),
    HIGHER_SECONDARY_SCHOOL("Higher Secondary School"),
    EMPTY("EMPTY");

    private final String dbValue;

    ClassLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return this == EMPTY ? "Unspecified" : dbValue;
    }

    /**
     * Null-safe parser that converts any raw string to a ClassLevel enum.
     *
     * @param text the input string
     * @return matching ClassLevel, or ClassLevel.EMPTY if null/unrecognized
     */
    public static ClassLevel fromString(String text) {
        if (text == null || text.isBlank() || text.equalsIgnoreCase("Choose")) {
            return EMPTY;
        }
        String clean = text.strip();
        for (ClassLevel level : values()) {
            if (level.dbValue.equalsIgnoreCase(clean) || level.name().equalsIgnoreCase(clean)) {
                return level;
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
        for (ClassLevel level : values()) {
            if (level.dbValue.equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }
}
