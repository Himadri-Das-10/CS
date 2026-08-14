package Features;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;
import Enums.RoomType;
import Enums.SeatingPreference;
import Enums.Sex;
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

    // Remembered from the most recent generateSeating call, so
    // renderSeating knows whether to draw aisle gaps without the
    // caller having to pass the room type twice.
    private RoomType roomType;

    // Loaded once and reused for every seat, rather than
    // re-reading the file from disk for each cell.
    private static final Image SEAT_ICON = loadSeatIcon();

    private static Image loadSeatIcon() {
        try
        {
            return new Image(
                    AllotStudents.class.getResourceAsStream("/Images/studentSeat.png")
            );
        }
        catch (Exception e)
        {
            System.out.println("Could not load seat icon image.");
            return null;
        }
    }


    // ---------------------------------------------------------
    // 1. Public entry point
    // ---------------------------------------------------------

    /**
     * Generates a full seating arrangement for the given students,
     * for the given room layout. Front is always the leftmost
     * column; back is always the rightmost column, regardless of
     * room type.
     *
     * Returns the list of Seats with students allocated, or null
     * if no valid arrangement exists.
     */
    public List<Seat> generateSeating(List<Student> students, int numberOfSeats, RoomType roomType) {

        this.roomType = roomType;

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
        int seatRows = (int) Math.ceil((double) numberOfSeats / cols);

        if (roomType == RoomType.COMPUTER_LAB && seatRows > 0)
        {
            // Interleave an aisle row between every pair of seat
            // rows, e.g. 4 seat rows -> seat, aisle, seat, aisle,
            // seat, aisle, seat (7 total grid rows).
            this.rows = (seatRows * 2) - 1;
        }
        else
        {
            this.rows = seatRows;
        }

        this.cols = cols;
    }


    // ---------------------------------------------------------
    // 3. Seat creation
    // ---------------------------------------------------------

    private List<Seat> createSeats() {

        List<Seat> seats = new ArrayList<>();

        for (int r = 0; r < rows; r++) {

            // In a Computer Lab, odd-indexed rows are aisles —
            // no seats are placed there at all.
            if (roomType == RoomType.COMPUTER_LAB && r % 2 != 0) {
                continue;
            }

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
        // Note: in a Computer Lab, seat rows are separated by an
        // aisle row (see createSeats), so two seats directly across
        // an aisle naturally have rowDiff = 2 and are NOT adjacent —
        // no special-casing needed here.
        return rowDiff <= 1 && colDiff <= 1;
    }


    // ---------------------------------------------------------
    // 7. Preference scoring (soft constraint: front/back)
    //
    // Front is always the leftmost column (col 0); back is always
    // the rightmost column (cols - 1) — for every room type.
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
        if (student == null) {
            return 0;
        }

        SeatingPreference pref = SeatingPreference.fromString(student.getSeatingPreference());

        return switch (pref) {
            case FRONT -> (cols - 1) - seat.getCol();
            case BACK -> seat.getCol();
            case EMPTY -> 0;
        };
    }


    // ---------------------------------------------------------
    // 8. Rendering — separate from allocation logic
    // ---------------------------------------------------------

    public void renderSeating(List<Seat> seats, javafx.scene.layout.GridPane gridPane) {

        gridPane.getChildren().clear();
        gridPane.getRowConstraints().clear();
        gridPane.getColumnConstraints().clear();

        // Give every row/column a fixed size so aisle rows (which
        // have no seat nodes in them) still render as a visible gap
        // instead of collapsing to zero height.
        for (int r = 0; r < rows; r++) {

            boolean isAisleRow =
                    roomType == RoomType.COMPUTER_LAB && r % 2 != 0;

            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(isAisleRow ? 30 : 90);
            rowConstraints.setPrefHeight(isAisleRow ? 30 : 90);

            gridPane.getRowConstraints().add(rowConstraints);
        }

        for (int c = 0; c < cols; c++) {

            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setMinWidth(90);
            colConstraints.setPrefWidth(90);

            gridPane.getColumnConstraints().add(colConstraints);
        }

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

        // Seat icon, shown in every cell whether occupied or not.
        if (SEAT_ICON != null)
        {
            ImageView iconView = new ImageView(SEAT_ICON);
            iconView.setFitWidth(48);
            iconView.setFitHeight(48);
            iconView.setPreserveRatio(true);

            pane.getChildren().add(iconView);
        }

        Student student = seat.getStudent();

        String labelText = (student != null) ? student.getName() : "";

        Label nameLabel = new Label(labelText);
        StackPane.setAlignment(nameLabel, Pos.BOTTOM_CENTER);

        pane.getChildren().add(nameLabel);

        return pane;
    }

    private String seatColour(Seat seat) {

        Student student = seat.getStudent();

        if (student == null) {
            return "lightgray";
        }

        Sex sex = Sex.fromString(student.getSex());

        return switch (sex) {
            case MALE -> "lightblue";
            case FEMALE -> "pink";
            case EMPTY -> "orange";
        };
    }
}


/**
 * Represents a single seat in the classroom grid.
 * Package-private: only AllotStudents.java can declare a *public*
 * top-level class matching the filename, so Seat stays package-private
 * and lives in the same file as before.
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