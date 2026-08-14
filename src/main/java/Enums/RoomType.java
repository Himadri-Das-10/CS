package Enums;

/**
 * Standardized representation of room types and venue configurations.
 */
public enum RoomType {
    CLASSROOM("Classroom"),
    AUDITORIUM("Auditorium"),
    COMPUTER_LAB("Computer Lab");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Null-safe parser that converts a string into a RoomType, defaulting to CLASSROOM.
     */
    public static RoomType fromString(String text) {
        if (text == null || text.isBlank() || text.equalsIgnoreCase("Setting")) {
            return CLASSROOM;
        }
        String clean = text.strip();
        for (RoomType type : values()) {
            if (type.displayName.equalsIgnoreCase(clean) || type.name().equalsIgnoreCase(clean)) {
                return type;
            }
        }
        return CLASSROOM;
    }
}
