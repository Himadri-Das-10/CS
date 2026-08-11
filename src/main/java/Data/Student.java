package Data;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Student {

    private String name;
    private String age;
    private String classLevel;
    private String division;
    private String sex;
    private String seatingPreference;
    private Image image;
    private List<Student> cannotSitWith;


    public static List<Student> students = new ArrayList<>();



    public Student(
            String name,
            String age,
            String classLevel,
            String division,
            String sex,
            String seatingPreference,
            Image image,
            List<Student> cannotSitWith
    ) {
        this.name = name;
        this.age = age;
        this.classLevel = classLevel;
        this.division = division;
        this.sex = sex;
        this.seatingPreference = seatingPreference;
        this.image = image;
        this.cannotSitWith = cannotSitWith;

        students.add(this);
    }



    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getClassLevel() {
        return classLevel;
    }

    public String getDivision() {
        return division;
    }

    public String getSex() {
        return sex;
    }

    public String getSeatingPreference() {
        return seatingPreference;
    }

    public Image getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setSeatingPreference(String seatingPreference) {
        this.seatingPreference = seatingPreference;
    }

    public void setImage(Image image) {
        this.image = image;
    }


    public List<Student> getCannotSitWith() {
        return cannotSitWith;
    }

    public void setCannotSitWith(List<Student> cannotSitWith) {
        this.cannotSitWith = cannotSitWith;
    }


    public static List<Student> getStudentsWithNamesMatching(String name)
    {
        List<Student> studentsWithNames = new ArrayList<>();
        for(Student student : students)
        {
            if(student.getName().equals(name))
            {
                studentsWithNames.add(student);
            }
        }

        return studentsWithNames;
    }


    public static List<Student> getStudentsWithNamesMatching(List<String> name)
    {
        List<Student> studentsWithNames = new ArrayList<>();
        for(Student student : students)
        {
            if(name.contains(student.getName()))
            {
                studentsWithNames.add(student);
            }
        }

        return studentsWithNames;
    }
}
