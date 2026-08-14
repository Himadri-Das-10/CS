package UI_Element;

import Backend.Backend;
import CODES.CODES;
import Controller.Home;
import Controller.MainPage;
import Features.Student;
import Offload.SeprateTask;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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
            VBox box,
            boolean addDB)
    {
        try
        {
            Parent card = buildCard(student);
            Platform.runLater(()->box.getChildren().add(card));

            if (addDB) {
                SeprateTask.getInstance().offload(() -> {
                    Backend.getInstance().addStudent(student, Student.userID);
                    Backend.getInstance().addCannotSitWith(student.getCannotSitWith(), student);
                });
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not create student card.");
            e.printStackTrace();
        }
    }




    public void createCard(File csvFile, VBox box)
    {
        System.out.println("Student.userID at import time: " + Student.userID);
        try
        {
            List<Student> students = new ArrayList<>();
            List<List<String>> pendingCannotSitWith = new ArrayList<>();

            /*
             * ============================================================
             * PHASE 1: READ CSV AND CREATE ALL STUDENT OBJECTS
             * ============================================================
             */

            try (BufferedReader reader =
                         new BufferedReader(new FileReader(csvFile)))
            {
                // Skip header
                String line = reader.readLine();

                while ((line = reader.readLine()) != null)
                {
                    if (line.isBlank())
                    {
                        continue;
                    }

                    // -1 preserves empty fields
                    String[] data = line.split(",", -1);

                    if (data.length != 8)
                    {
                        System.out.println("Invalid CSV row: " + line);
                        continue;
                    }

                    String name = data[0].strip();
                    String age = data[1].strip();
                    String classLevel = data[2].strip();
                    String division = data[3].strip();
                    String sex = data[4].strip();
                    String seatingPreference = data[5].strip();
                    String cannotSitWithData = data[6].strip();
                    String imagePath = data[7].strip();

                    /*
                     * Convert EMPTY image path into null.
                     */
                    Image image = null;

                    if (!imagePath.equalsIgnoreCase("EMPTY")
                            && !imagePath.isBlank())
                    {
                        image = new Image(
                                Paths.get(imagePath)
                                        .toUri()
                                        .toString()
                        );
                    }

                    /*
                     * Store cannot_sit_with names for later.
                     *
                     * We CANNOT resolve these to database IDs yet
                     * because some students may appear later in the CSV.
                     */
                    List<String> cannotSitWithNames = new ArrayList<>();

                    if (!cannotSitWithData.isBlank()
                            && !cannotSitWithData.equalsIgnoreCase("EMPTY"))
                    {
                        for (String rawName :
                                cannotSitWithData.split(";"))
                        {
                            String trimmed = rawName.strip();

                            if (!trimmed.isEmpty())
                            {
                                cannotSitWithNames.add(trimmed);
                            }
                        }
                    }

                    /*
                     * Create the Student object.
                     *
                     * At this point dbID will not necessarily be valid.
                     */
                    Student student = new Student(
                            name,
                            age,
                            classLevel,
                            division,
                            sex,
                            seatingPreference,
                            image,
                            new ArrayList<>()
                    );

                    students.add(student);
                    pendingCannotSitWith.add(cannotSitWithNames);
                }
            }


            /*
             * ============================================================
             * PHASE 2: INSERT EVERY STUDENT INTO DATABASE
             * ============================================================
             *
             * This MUST happen before adding cannot_sit_with relationships.
             *
             * Every Student must have a valid dbID first.
             */



            for (Student student : students)
            {
                Backend.getInstance().addStudent(student, Student.userID);

                System.out.println(
                        "Inserted " +
                                student.getName() +
                                " with DB ID " +
                                student.getDbID()
                );
            }


            /*
             * ============================================================
             * PHASE 3: RESOLVE cannot_sit_with RELATIONSHIPS
             * ============================================================
             */

            for (int i = 0; i < students.size(); i++)
            {
                Student student = students.get(i);

                List<String> names =
                        pendingCannotSitWith.get(i);

                List<Student> resolved =
                        new ArrayList<>();

                for (String targetName : names)
                {
                    Student match =
                            findByName(students, targetName);

                    if (match != null)
                    {
                        /*
                         * At this point match MUST already have
                         * a database ID.
                         */
                        resolved.add(match);
                    }
                    else
                    {
                        System.out.println(
                                "Warning: '" +
                                        student.getName() +
                                        "' has cannot_sit_with entry '" +
                                        targetName +
                                        "' which was not found."
                        );
                    }
                }

                student.setCannotSitWith(resolved);
            }


            /*
             * ============================================================
             * PHASE 4: INSERT cannot_sit_with RELATIONSHIPS
             * ============================================================
             */

            for (Student student : students)
            {
                System.out.println(
                        "Adding cannot-sit-with relationships for " +
                                student.getName() +
                                " (ID " +
                                student.getDbID() +
                                ")"
                );

                Backend.getInstance().addCannotSitWith(
                        student.getCannotSitWith(),
                        student
                );
            }


            /*
             * ============================================================
             * PHASE 5: DISPLAY THE CARDS
             * ============================================================
             */

            for (Student student : students)
            {
                Parent card = buildCard(student);

                Platform.runLater(() ->
                {
                    box.getChildren().add(card);

                    Label guestNumLabel =
                            MainPage.getInstance()
                                    .getGuestNumLabel();

                    guestNumLabel.setText(
                            String.valueOf(
                                    Integer.parseInt(
                                            guestNumLabel
                                                    .getText()
                                                    .strip()
                                    ) + 1
                            )
                    );
                });
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not read student CSV.");

            Platform.runLater(() ->
                    MainPage.getInstance()
                            .getErrorPane()
                            .setVisible(true)
            );

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
            Student std = (Student) card.getUserData();
            Platform.runLater(() -> {



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
                    Cards.getInstance().createCard(stu, studentVBox, false);
                }

                MainPage.getInstance().getGuestNumLabel().setText(
                        String.valueOf(
                                Integer.parseInt(
                                        MainPage.getInstance().getGuestNumLabel().getText().strip()
                                ) - 1
                        )
                );
            });


            Backend.getInstance().deleteStudent(std.getDbID());
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


        nameText.setText("Name: " + Enums.EnumMapper.formatForDisplay(student.getName()));
        ageText.setText("Age: " + Enums.EnumMapper.formatForDisplay(student.getAge()));
        classText.setText("Class: " + Enums.EnumMapper.formatForDisplay(student.getClassLevel()));
        divisionText.setText("Division: " + Enums.EnumMapper.formatForDisplay(student.getDivision()));
        sexText.setText("Sex: " + Enums.EnumMapper.formatForDisplay(student.getSex()));

        Pane sexStrip = (Pane) loader.getNamespace().get("sexStrip");
        Enums.Sex sex = Enums.Sex.fromString(student.getSex());
        switch (sex) {
            case MALE -> sexStrip.setStyle("-fx-background-color: #536DFE;"); // Lapis blue
            case FEMALE -> sexStrip.setStyle("-fx-background-color: #E8A0BF;"); // Bougainvillea pink
            case EMPTY -> sexStrip.setStyle("-fx-background-color: lightgray;");
        }

        // Set the student's image if one was selected.
        if (student.getImage() != null)
        {

            studentImage.setImage(student.getImage());
        }





        return card;
    }










}