package UI_Element;

import Controller.MainPage;
import Features.Student;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import CODES.CODES;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cards
{
    private static Cards cards;
    private Cards(){}
    public static Cards getInstance()
    {
        if(cards==null)
        {
            cards = new Cards();
        }
        return cards;
    }



    public void createCard(
            Student student,
            VBox box)
    {
        try
        {
            Parent card = buildCard(student);
            Platform.runLater(()->box.getChildren().add(card));
        }
        catch (IOException e)
        {
            System.out.println("Could not create student card.");
            e.printStackTrace();
        }
    }




    public void createCard(File csvFile, VBox box)
    {
        try
        {
            List<Student> students = new ArrayList<>();
            List<List<String>> pendingCannotSitWith = new ArrayList<>();

            // Open the CSV file for reading.
            try (BufferedReader reader =
                         new BufferedReader(new FileReader(csvFile)))
            {
                String line = reader.readLine(); // skip header

                // First pass: build every Student, and remember the raw
                // cannot_sit_with names as strings (can't resolve them
                // to Student objects yet, since some may not exist until
                // a later row in the file).
                while ((line = reader.readLine()) != null)
                {
                    if (line.isBlank())
                    {
                        continue;
                    }

                    // Split the CSV row into individual fields.
                    String[] data = line.split(",");

                    String name = data[0].strip();
                    String age = data[1].strip();
                    String classLevel = data[2].strip();
                    String division = data[3].strip();
                    String sex = data[4].strip();
                    String seatingPreference = data[5].strip();

                    // cannot_sit_with names are separated by ';' within
                    // the field, to avoid colliding with the ',' that
                    // separates CSV columns.
                    List<String> cannotSitWithNames = new ArrayList<>();

                    if (data.length > 6 && !data[6].isBlank())
                    {
                        for (String rawName : data[6].split(";"))
                        {
                            String trimmed = rawName.strip();

                            if (!trimmed.isEmpty())
                            {
                                cannotSitWithNames.add(trimmed);
                            }
                        }
                    }

                    Student student = new Student(
                            name, age, classLevel, division, sex,
                            seatingPreference, null, Student.getStudentsWithNamesMatching(cannotSitWithNames)
                    );

                    students.add(student);
                    pendingCannotSitWith.add(cannotSitWithNames);
                }
            }

            // Second pass: now that every Student exists, resolve each
            // student's cannot_sit_with names into actual Student
            // references and attach them.
            for (int i = 0; i < students.size(); i++)
            {
                Student student = students.get(i);
                List<String> names = pendingCannotSitWith.get(i);

                List<Student> resolved = new ArrayList<>();

                for (String targetName : names)
                {
                    Student match = findByName(students, targetName);

                    if (match != null)
                    {
                        resolved.add(match);
                    }
                    else
                    {
                        System.out.println(
                                "Warning: '" + student.getName()
                                        + "' has cannot_sit_with entry '"
                                        + targetName + "' which was not found."
                        );
                    }
                }

                student.setCannotSitWith(resolved);
            }

            // Third pass: build and add a card for every student.
            for (Student student : students)
            {
                Parent card = buildCard(student);

                Platform.runLater(() -> {
                    box.getChildren().add(card);

                    Label guestNumLabel = MainPage.getInstance().getGuestNumLabel();
                    guestNumLabel.setText(String.valueOf(
                            Integer.parseInt(guestNumLabel.getText().strip()) + 1
                    ));
                });
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not read student CSV.");
            Platform.runLater(()-> MainPage.getInstance().getErrorPane().setVisible(true));
            e.printStackTrace();
        }
    }


    /**
     * Finds a student by name within the given list.
     * Used to resolve cannot_sit_with names to Student references.
     */
    private Student findByName(List<Student> students, String name)
    {
        for (Student student : students)
        {
            if (student.getName().equalsIgnoreCase(name))
            {
                return student;
            }
        }

        return null;
    }



    /**
     * Loads the studentCard FXML, populates it with the given
     * student's information, and returns the completed card.
     * Shared by both createCard overloads to avoid duplicating
     * the FXML-loading and field-population logic.
     */
    private Parent buildCard(Student student) throws IOException
    {
        // Load the student card FXML.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/FXMLfiles/studentCard.fxml")
        );

        // Load the FXML and obtain the card as a Parent.
        Parent card = loader.load();

        card.setUserData(student);


        ContextMenu contextMenu = new ContextMenu();


        MenuItem detailsItem = new MenuItem("Details");
        MenuItem deleteItem = new MenuItem("Delete");

        contextMenu.getItems().addAll(
                detailsItem,
                deleteItem
        );

        card.setOnContextMenuRequested(event -> {

            contextMenu.show(
                    card,
                    event.getScreenX(),
                    event.getScreenY()
            );

            event.consume();
        });



        detailsItem.setOnAction(event -> {

            Student std =
                    (Student) card.getUserData();

            MainPage.getInstance().showStudentDetails(std);

        });

        deleteItem.setOnAction(event -> {
            Platform.runLater(() -> {

                Student std = (Student) card.getUserData();

                // Remove std from every other student's cannotSitWith list.
                for (Student stu : Student.students)
                {
                    stu.getCannotSitWith().remove(std);
                }

                // Remove std from the master list and from the separation menu.
                Student.students.remove(std);
                CheckMenu.getInstance().removeCheckMenuItem(std, MainPage.getInstance().getSeprationMenu());

                // Rebuild the whole VBox from the now-updated Student.students,
                // instead of patching individual cards in place — this avoids
                // the ConcurrentModificationException entirely and guarantees
                // every card reflects the current cannotSitWith state.
                VBox studentVBox = MainPage.getInstance().getStudentVBox();
                studentVBox.getChildren().clear();

                for (Student stu : Student.students)
                {
                    Cards.getInstance().createCard(stu, studentVBox);
                }

                MainPage.getInstance().getGuestNumLabel().setText(
                        String.valueOf(
                                Integer.parseInt(
                                        MainPage.getInstance().getGuestNumLabel().getText().strip()
                                ) - 1
                        )
                );
            });
        });

        // Retrieve the UI elements using their fx:id.
        ImageView studentImage =
                (ImageView) loader.getNamespace().get("studentImage");

        Text nameText =
                (Text) loader.getNamespace().get("nameText");

        Text ageText =
                (Text) loader.getNamespace().get("ageText");

        Text classText =
                (Text) loader.getNamespace().get("classText");

        Text divisionText =
                (Text) loader.getNamespace().get("divisionText");

        Text sexText =
                (Text) loader.getNamespace().get("sexText");


        // Display "Unspecified" when the value is CODES.EMPTY.
        String empty = String.valueOf(CODES.EMPTY);

        nameText.setText(
                "Name: " +
                        (student.getName().equals(empty) ? "Unspecified" : student.getName())
        );

        ageText.setText(
                "Age: " +
                        (student.getAge().equals(empty) ? "Unspecified" : student.getAge())
        );

        classText.setText(
                "Class: " +
                        (student.getClassLevel().equals(empty) ? "Unspecified" : student.getClassLevel())
        );

        divisionText.setText(
                "Division: " +
                        (student.getDivision().equals(empty) ? "Unspecified" : student.getDivision())
        );

        sexText.setText(
                "Sex: " +
                        (student.getSex().equals(empty) ? "Unspecified" : student.getSex())
        );

        Pane sexStrip =
                (Pane) loader.getNamespace().get("sexStrip");

        if (student.getSex().equalsIgnoreCase("Male"))
        {
            sexStrip.setStyle("-fx-background-color: #536DFE;"); // Lapis blue
        }
        else if (student.getSex().equalsIgnoreCase("Female"))
        {
            sexStrip.setStyle("-fx-background-color: #E8A0BF;"); // Bougainvillea pink
        }

        // Set the student's image if one was selected.
        if (student.getImage() != null)
        {
            studentImage.setImage(student.getImage());
        }

        return card;
    }




}