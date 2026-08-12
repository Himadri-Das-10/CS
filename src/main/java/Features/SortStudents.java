package Features;

import UI_Element.Cards;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static Features.Student.students;
public class SortStudents
{
    private static SortStudents sortStudents;
    private SortStudents(){}
    public static  SortStudents getInstance(){
        if(sortStudents == null){
            sortStudents = new SortStudents();
        }
        return sortStudents;
    }






    public void sortStudents(VBox container)
    {
        //Shallow copy: List altering will not affect 'students' but
        // altering the elements will affect the Student objects.
        List<Student> copy = new ArrayList<>(students);

        copy = mergeSort(copy);

        for(Student student : copy){
            Cards.getInstance().createCard(student, container);

        }



    }




    public List<Student> mergeSort(List<Student> copy)
    {
        // Base case: a list of 0 or 1 elements is already sorted.
        if (copy.size() <= 1)
        {
            return copy;
        }

        int middle = copy.size() / 2;

        // Split the list into left and right halves.
        List<Student> left = new java.util.ArrayList<>(copy.subList(0, middle));
        List<Student> right = new java.util.ArrayList<>(copy.subList(middle, copy.size()));

        // Recursively sort each half.
        left = mergeSort(left);
        right = mergeSort(right);

        // Merge the two sorted halves back together.
        return merge(left, right);
    }


    /**
     * Merges two already-sorted lists into one sorted list,
     * comparing students alphabetically by name.
     */
    private List<Student> merge(List<Student> left, List<Student> right)
    {
        List<Student> result = new java.util.ArrayList<>();

        int i = 0;
        int j = 0;

        // Repeatedly take the smaller of the two current elements.
        while (i < left.size() && j < right.size())
        {
            String leftName = left.get(i).getName().toLowerCase();
            String rightName = right.get(j).getName().toLowerCase();

            // Using <= (not <) keeps the sort stable: if names are
            // equal, the element from the left list (which came
            // first in the original order) is placed first.
            if (leftName.compareTo(rightName) <= 0)
            {
                result.add(left.get(i));
                i++;
            }
            else
            {
                result.add(right.get(j));
                j++;
            }
        }

        // Append any remaining elements from the left list.
        while (i < left.size())
        {
            result.add(left.get(i));
            i++;
        }

        // Append any remaining elements from the right list.
        while (j < right.size())
        {
            result.add(right.get(j));
            j++;
        }

        return result;
    }

}
