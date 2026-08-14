package Enums;

/**
 * Standardized representation of classroom divisions matching PostgreSQL division_enum.
 */
public enum Division {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    EMPTY("EMPTY");

    private final String dbValue;

    Division(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return this == EMPTY ? "Unspecified" : dbValue;
    }

    /**
     * Null-safe parser that converts any raw string to a Division enum.
     *
     * @param text the input string
     * @return matching Division, or Division.EMPTY if null/unrecognized
     */
    public static Division fromString(String text) {
        if (text == null || text.isBlank() || text.equalsIgnoreCase("Choose")) {
            return EMPTY;
        }
        String clean = text.strip();
        for (Division div : values()) {
            if (div.dbValue.equalsIgnoreCase(clean) || div.name().equalsIgnoreCase(clean)) {
                return div;
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
        for (Division div : values()) {
            if (div.dbValue.equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }
}
