package Features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Features.Student.students;

public class SearchStudents
{
    private static SearchStudents searchStudents;
    private SearchStudents(){}
    public static SearchStudents getInstance()
    {
        if(searchStudents==null)
        {
            searchStudents = new SearchStudents();
        }
        return searchStudents;
    }

    public static List<Student> studentSearch(String name1)
    {
        if(name1.isEmpty())
        {
            return students;
        }
        List<Student> result=new ArrayList<>();

        name1=name1.toLowerCase().strip();
        for(Student s:students)
        {
            String name2 = s.getName().toLowerCase().strip();
            double simi = jaccardSimilarity(name1,name2);

            if(simi>0.35)
                result.add(s);


        }

        return result;
    }



    /**
     * Calculates the Jaccard similarity between two strings.
     *
     * Jaccard similarity is calculated as:
     *
     *       |A ∩ B|
     * J = -----------
     *       |A ∪ B|
     *
     * A = unique characters in the first string
     * B = unique characters in the second string
     *
     * The result is between 0 and 1.
     *
     * 1.0 = identical character sets
     * 0.0 = no characters in common
     */
    public static double jaccardSimilarity(String first, String second)
    {


        // Create sets containing the unique characters
        // from each string.
        Set<Character> firstSet = new HashSet<>();
        Set<Character> secondSet = new HashSet<>();

        // Add every character from the first string.
        for (char character : first.toCharArray())
        {
            firstSet.add(character);
        }

        // Add every character from the second string.
        for (char character : second.toCharArray())
        {
            secondSet.add(character);
        }

        // Create the intersection.
        // This contains characters that occur in BOTH strings.
        Set<Character> intersection = new HashSet<>(firstSet);
        intersection.retainAll(secondSet);

        // Create the union.
        // This contains every unique character from either string.
        Set<Character> union = new HashSet<>(firstSet);
        union.addAll(secondSet);

        // Avoid division by zero if both strings are empty.
        if (union.isEmpty())
        {
            return 1.0;
        }

        // Jaccard similarity = intersection / union.
        return (double) intersection.size() / union.size();
    }
}
