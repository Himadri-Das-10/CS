package Features;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete strategy that partitions students by gender into Boys, Girls, and Unspecified groups,
 * sorting each partition alphabetically using Merge Sort.
 */
public class GenderPartitionStrategy implements StudentArrangementStrategy {

    private final AlphabeticalSortStrategy sorter = new AlphabeticalSortStrategy();

    @Override
    public List<Student> arrange(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }

        List<Student> boys = new ArrayList<>();
        List<Student> girls = new ArrayList<>();
        List<Student> unspecified = new ArrayList<>();

        // Partition students by gender
        for (Student student : students) {
            String sex = student.getSex();
            if ("Male".equalsIgnoreCase(sex)) {
                boys.add(student);
            } else if ("Female".equalsIgnoreCase(sex)) {
                girls.add(student);
            } else {
                unspecified.add(student);
            }
        }

        // Sort each partition alphabetically and combine
        List<Student> result = new ArrayList<>();
        result.addAll(sorter.arrange(boys));
        result.addAll(sorter.arrange(girls));
        result.addAll(sorter.arrange(unspecified));

        return result;
    }

    @Override
    public String getName() {
        return "Separate Boys and Girls";
    }
}
