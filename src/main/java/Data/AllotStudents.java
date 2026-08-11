package Data;


import Data.Student;

import java.util.*;

public class AllotStudents {

    private static AllotStudents allotStudents;
    private AllotStudents() {}

    public static AllotStudents getInstance() {
        if (allotStudents == null) {
            allotStudents = new AllotStudents();
        }
        return allotStudents;
    }

    private final Random random = new Random();

    private int rows;
    private int cols;


    // ---------------------------------------------------------
    // 1. Public entry point
    // ---------------------------------------------------------

    /**
     * Generates a full seating arrangement for the given students.
     * Returns the list of Seats with students allocated, or null
     * if no valid arrangement exists.
     */
    public List<Seat> generateSeating(List<Student> students, int numberOfSeats) {

        calculateDimensions(numberOfSeats);

        List<Seat> seats = createSeats();

        List<Student> ordered = orderByRestrictions(students);

        boolean success = allocate(ordered, 0, seats);

        return success ? seats : null;
    }


    // ---------------------------------------------------------
    // 2. Grid dimensions — as close to square as possible
    // ---------------------------------------------------------

    private void calculateDimensions(int numberOfSeats) {

        int cols = (int) Math.ceil(Math.sqrt(numberOfSeats));
        int rows = (int) Math.ceil((double) numberOfSeats / cols);

        this.rows = rows;
        this.cols = cols;
    }


    // ---------------------------------------------------------
    // 3. Seat creation
    // ---------------------------------------------------------

    private List<Seat> createSeats() {

        List<Seat> seats = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                seats.add(new Seat(r, c));
            }
        }

        return seats;
    }


    // ---------------------------------------------------------
    // 4. Student ordering — most restricted first, ties randomized
    // ---------------------------------------------------------

    private List<Student> orderByRestrictions(List<Student> students) {

        List<Student> ordered = new ArrayList<>(students);

        // Shuffle first so that students with equal restriction
        // counts end up in random relative order after the
        // stable sort below.
        Collections.shuffle(ordered, random);

        ordered.sort((a, b) ->
                Integer.compare(
                        b.getCannotSitWith().size(),
                        a.getCannotSitWith().size()
                )
        );

        return ordered;
    }


    // ---------------------------------------------------------
    // 5. Backtracking allocation
    // ---------------------------------------------------------

    private boolean allocate(List<Student> students, int index, List<Seat> seats) {

        // All students placed successfully.
        if (index == students.size()) {
            return true;
        }

        Student student = students.get(index);

        List<Seat> validSeats = findValidSeats(student, seats);

        if (validSeats.isEmpty()) {
            return false;
        }

        List<Seat> bestSeats = highestScoringSeats(student, validSeats);

        // Try best-scoring seats first (in random order among ties),
        // but fall back to trying all valid seats if none of the
        // best-scoring ones lead to a full valid arrangement.
        List<Seat> tryOrder = new ArrayList<>(bestSeats);
        Collections.shuffle(tryOrder, random);

        for (Seat seat : validSeats) {
            if (!tryOrder.contains(seat)) {
                tryOrder.add(seat);
            }
        }

        for (Seat seat : tryOrder) {

            seat.setStudent(student);

            if (allocate(students, index + 1, seats)) {
                return true;
            }

            // Backtrack: undo this placement and try the next seat.
            seat.setStudent(null);
        }

        return false;
    }


    // ---------------------------------------------------------
    // 6. Finding valid seats (hard constraint: cannotSitWith)
    // ---------------------------------------------------------

    private List<Seat> findValidSeats(Student student, List<Seat> seats) {

        List<Seat> valid = new ArrayList<>();

        for (Seat seat : seats) {

            if (seat.isEmpty() && !violatesCannotSitWith(student, seat, seats)) {
                valid.add(seat);
            }
        }

        return valid;
    }

    private boolean violatesCannotSitWith(Student student, Seat candidate, List<Seat> seats) {

        for (Seat seat : seats) {

            if (seat.isEmpty()) {
                continue;
            }

            if (!isAdjacent(candidate, seat)) {
                continue;
            }

            Student neighbour = seat.getStudent();

            // Check both directions of the restriction.
            if (student.getCannotSitWith().contains(neighbour)
                    || neighbour.getCannotSitWith().contains(student)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAdjacent(Seat a, Seat b) {

        int rowDiff = Math.abs(a.getRow() - b.getRow());
        int colDiff = Math.abs(a.getCol() - b.getCol());

        // Same seat is not "adjacent" to itself.
        if (rowDiff == 0 && colDiff == 0) {
            return false;
        }

        // Adjacent = within one row and one column in any direction,
        // covering horizontal, vertical, and diagonal neighbours.
        return rowDiff <= 1 && colDiff <= 1;
    }


    // ---------------------------------------------------------
    // 7. Preference scoring (soft constraint: front/back)
    // ---------------------------------------------------------

    private List<Seat> highestScoringSeats(Student student, List<Seat> validSeats) {

        int bestScore = Integer.MIN_VALUE;
        List<Seat> best = new ArrayList<>();

        for (Seat seat : validSeats) {

            int score = preferenceScore(student, seat);

            if (score > bestScore) {
                bestScore = score;
                best.clear();
                best.add(seat);
            } else if (score == bestScore) {
                best.add(seat);
            }
        }

        return best;
    }

    private int preferenceScore(Student student, Seat seat) {

        String preference = student.getSeatingPreference();

        if (preference == null) {
            return 0;
        }

        preference = preference.strip().toLowerCase();

        // Higher score = closer to the preferred edge of the grid.
        // Row 0 is the front row; row (rows - 1) is the back row.
        switch (preference) {

            case "front":
                return (rows - 1) - seat.getRow();

            case "back":
                return seat.getRow();

            default:
                // No preference / unrecognised value: neutral score
                // so it never outranks a genuine preference match.
                return 0;
        }
    }


    // ---------------------------------------------------------
    // 8. Rendering — separate from allocation logic
    // ---------------------------------------------------------

    public void renderSeating(List<Seat> seats, javafx.scene.layout.GridPane gridPane) {

        gridPane.getChildren().clear();
        gridPane.getRowConstraints().clear();
        gridPane.getColumnConstraints().clear();

        for (Seat seat : seats) {

            javafx.scene.layout.StackPane seatPane = createSeatNode(seat);

            gridPane.add(seatPane, seat.getCol(), seat.getRow());
        }
    }

    private javafx.scene.layout.StackPane createSeatNode(Seat seat) {

        javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane();

        pane.setPrefSize(80, 80);
        pane.setStyle(
                "-fx-border-color: black; -fx-border-width: 1; "
                        + "-fx-background-color: " + seatColour(seat) + ";"
        );

        Student student = seat.getStudent();

        String label = (student != null) ? student.getName() : "";

        pane.getChildren().add(new javafx.scene.control.Label(label));

        return pane;
    }

    private String seatColour(Seat seat) {

        Student student = seat.getStudent();

        if (student == null) {
            return "lightgray";
        }

        String sex = student.getSex();

        if (sex == null) {
            return "orange";
        }

        switch (sex.strip().toLowerCase()) {
            case "male":
                return "lightblue";
            case "female":
                return "pink";
            default:
                return "orange";
        }
    }
}




/**
 * Represents a single seat in the classroom grid.
 */
class Seat {

    private final int row;
    private final int col;
    private Student student;

    public Seat(int row, int col) {
        this.row = row;
        this.col = col;
        this.student = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public boolean isEmpty() {
        return student == null;
    }
}