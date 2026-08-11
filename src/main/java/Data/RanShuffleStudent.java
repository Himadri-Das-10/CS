package Data;

import UI_Element.Cards;
import javafx.scene.layout.VBox;

import java.util.Random;
import static Data.Student.students;

public class RanShuffleStudent
{
    private static RanShuffleStudent ranShuffleStudent;
    private RanShuffleStudent(){}

    public static RanShuffleStudent getInstance(){
        if(ranShuffleStudent == null){
            ranShuffleStudent = new RanShuffleStudent();
        }
        return ranShuffleStudent;
    }

    public void ranShuffleStudent(VBox container){

            Random random = new Random();

            for (int i = students.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);

                Student temp = students.get(i);
                students.set(i,students.get(j));
                students.set(j,temp);
            }

            for(Student student : students){
                Cards.getInstance().createCard(student, container);
            }

    }
}
