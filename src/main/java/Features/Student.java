package Features;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Student {

    private String name;
    private String age;
    private String classLevel;
    private String division;
    private String sex;
    private String seatingPreference;
    private Image image;
    private List<Student> cannotSitWith;
    private int dbID;

    public static int userID;


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



    public List<Student> getCannotSitWith() {
        return cannotSitWith;
    }

    public void setCannotSitWith(List<Student> cannotSitWith) {
        this.cannotSitWith = cannotSitWith;
    }







    public int getDbID() {
        return dbID;
    }

    public void setDbID(int dbID) {
        this.dbID = dbID;
    }
}
