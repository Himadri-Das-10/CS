package Features;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete strategy that sorts students alphabetically by name using a custom
 * recursive Merge Sort algorithm ($O(N \log N)$ time complexity).
 */
public class AlphabeticalSortStrategy implements StudentArrangementStrategy {

    @Override
    public List<Student> arrange(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }
        return mergeSort(new ArrayList<>(students));
    }

    @Override
    public String getName() {
        return "Sort (Alphabetical)";
    }

    /**
     * Recursively divides the list into sublists and merges them in sorted order.
     *
     * @param list the list of students to sort
     * @return sorted list of students
     */
    public List<Student> mergeSort(List<Student> list) {
        // Base case: a list of 0 or 1 elements is already sorted
        if (list.size() <= 1) {
            return list;
        }

        int middle = list.size() / 2;

        // Split into left and right sublists
        List<Student> left = new ArrayList<>(list.subList(0, middle));
        List<Student> right = new ArrayList<>(list.subList(middle, list.size()));

        // Recursively sort each half
        left = mergeSort(left);
        right = mergeSort(right);

        // Merge the two sorted halves
        return merge(left, right);
    }

    /**
     * Merges two sorted lists into one sorted list comparing students alphabetically.
     */
    private List<Student> merge(List<Student> left, List<Student> right) {
        List<Student> result = new ArrayList<>();
        int i = 0;
        int j = 0;

        // Repeatedly take the smaller element
        while (i < left.size() && j < right.size()) {
            String leftName = (left.get(i).getName() == null) ? "" : left.get(i).getName().toLowerCase();
            String rightName = (right.get(j).getName() == null) ? "" : right.get(j).getName().toLowerCase();

            // Using <= preserves stability of the sort
            if (leftName.compareTo(rightName) <= 0) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }

        // Append remaining elements
        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }

        return result;
    }
}
