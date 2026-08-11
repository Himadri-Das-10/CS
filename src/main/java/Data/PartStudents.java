package Data;
import UI_Element.Cards;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static Data.Student.students;

public class PartStudents
{
    private static PartStudents partStudents;
    private PartStudents(){}

    public static PartStudents getInstance()
    {
        if(partStudents==null)
            partStudents=new PartStudents();
        return partStudents;
    }

    public void partStudents(VBox container)
    {
        List<Student> boys = new ArrayList<>();
        List<Student> girls = new ArrayList<>();
        List<Student> none = new ArrayList<>();

        // Partition students into boys and girls.
        for (Student student : students)
        {
            if (student.getSex().equalsIgnoreCase("Male"))
            {
                boys.add(student);
            }
            else if (student.getSex().equalsIgnoreCase("Female"))
            {
                girls.add(student);
            }
            else
            {
                none.add(student);
            }
        }

        // Sort both groups using your own merge sort.
        SortStudents sorter = SortStudents.getInstance();

        boys = sorter.mergeSort(boys);
        girls = sorter.mergeSort(girls);
        none = sorter.mergeSort(none);




        for (Student student : boys)
        {
            Cards.getInstance().createCard(student, container);
        }


        for (Student student : girls)
        {
            Cards.getInstance().createCard(student, container);
        }

        for (Student student : none)
        {
            Cards.getInstance().createCard(student, container);
        }
    }
}
