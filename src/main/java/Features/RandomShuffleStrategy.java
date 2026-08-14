package Features;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Concrete strategy that shuffles students randomly using the Fisher-Yates algorithm
 * ($O(N)$ time complexity, unbiased uniform permutation).
 */
public class RandomShuffleStrategy implements StudentArrangementStrategy {

    private final Random random = new Random();

    @Override
    public List<Student> arrange(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }

        List<Student> result = new ArrayList<>(students);

        // Fisher-Yates shuffle
        for (int i = result.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            Student temp = result.get(i);
            result.set(i, result.get(j));
            result.set(j, temp);
        }

        return result;
    }

    @Override
    public String getName() {
        return "Shuffle Students";
    }
}
