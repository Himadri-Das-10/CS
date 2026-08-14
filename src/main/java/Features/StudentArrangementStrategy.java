package Features;

import java.util.List;

/**
 * Strategy Interface for organizing and arranging student collections.
 *
 * Part of the Gang of Four (GoF) Strategy Design Pattern. Encapsulates interchangeable
 * sorting, shuffling, and partitioning algorithms behind a uniform polymorphic contract.
 */
public interface StudentArrangementStrategy {

    /**
     * Reorganizes the provided student list according to the concrete strategy.
     *
     * @param students the list of students to arrange
     * @return a new arranged list of Student objects
     */
    List<Student> arrange(List<Student> students);

    /**
     * Returns the human-readable name of this strategy for UI labels.
     *
     * @return strategy display name
     */
    String getName();
}
