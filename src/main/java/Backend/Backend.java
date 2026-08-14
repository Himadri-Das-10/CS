package Backend;

import CODES.CODES;
import Controller.Home;
import Controller.MainPage;
import Features.Student;
import Offload.SeprateTask;
import UI_Element.Cards;
import UI_Element.CheckMenu;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class Backend {

    private static Backend backend;

    private Backend(){}



    // Getters
    public static Backend getInstance() {
        if (backend == null) {
            backend = new Backend();
        }
        return backend;
    }










    // Checks whether the email exists and whether
    // the username belongs to that email.
    public CODES checkUser(String email, String username) {

        String sql = """
                SELECT username
                FROM Users
                WHERE email = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            // Email does not exist
            if (!resultSet.next()) {
                return CODES.EMAILDNE;
            }

            // Email exists, check username
            String storedUsername = resultSet.getString("username");

            if (!storedUsername.equals(username)) {
                return CODES.NAMEERR;
            }

            // Both email and username are correct
            return CODES.SUCCESS;

        } catch (SQLException e) {
            System.out.println("Could not check whether user exists or not");
            return null;
        }
    }


    public void addUser(String email, String username) {

        // SQL query used to insert a new user into the database.
        // The '?' placeholders are replaced with the actual values
        // using the PreparedStatement below.
        String sql = """
            INSERT INTO Users (email, username)
            VALUES (?, ?)
            RETURNING user_id;
            """;




        // Database operations can take time because they involve
        // communication with an external database server.
        // Therefore, the operation is moved to a separate thread
        // so that the JavaFX Application Thread is not blocked.




            // Opens a database connection and creates a PreparedStatement.
            // The try-with-resources statement automatically closes both
            // resources when they are no longer needed.
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                // Assigns the email to the first '?' placeholder.
                statement.setString(1, email);

                // Assigns the username to the second '?' placeholder.
                statement.setString(2, username);


                // Executes the INSERT query and adds the user
                // to the Users table.
                ResultSet es = statement.executeQuery();

                if (es.next()) {
                    Student.userID = es.getInt("user_id");
                }


            } catch (SQLException e) {

                // Handles errors that occur while establishing the
                // database connection or creating the PreparedStatement.
                System.out.println("Could not add user");
                e.printStackTrace();
            }

    }











    public void changeUserName(String username, String email) {

        String sql = """
        UPDATE Users
        SET username = ?
        WHERE email = ?
        """;

        SeprateTask.getInstance().offload(() -> {

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, email);

                int rowsUpdated = statement.executeUpdate();

                System.out.println("Rows updated: " + rowsUpdated);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }



    public void addStudent(Student data, int user_id) {

        // SQL query used to insert a new user into the database.
        // The '?' placeholders are replaced with the actual values
        // using the PreparedStatement below.
        String sql = """
        INSERT INTO students
        (user_id, name, age, class_level, division, sex, seating_preference, img_path)
        VALUES (?, ?, ?, ?::class_level_enum, ?::division_enum, ?::sex_enum, ?::seating_pref_enum, ?)
        RETURNING student_id
    """;




        // Database operations can take time because they involve
        // communication with an external database server.
        // Therefore, the operation is moved to a separate thread
        // so that the JavaFX Application Thread is not blocked.




            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, user_id);
                statement.setString(2, data.getName());
                statement.setInt(3, (data.getAge() == null || data.getAge().equals("EMPTY")) ? -1 : Integer.parseInt(data.getAge()));

                // Standardized Enum mappings with efficient null handling
                statement.setString(4, Enums.EnumMapper.toClassLevelDb(data.getClassLevel()));
                statement.setString(5, Enums.EnumMapper.toDivisionDb(data.getDivision()));
                statement.setString(6, Enums.EnumMapper.toSexDb(data.getSex()));
                statement.setString(7, Enums.EnumMapper.toSeatingPrefDb(data.getSeatingPreference()));

                statement.setString(8, (data.getImage() == null) ? "EMPTY" : data.getImage().getUrl());

                // Executes the INSERT query and retrieves the generated student_id
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    data.setDbID(resultSet.getInt("student_id"));
                }

            } catch (SQLException e) {
                System.err.println("Could not add student to database");
                e.printStackTrace();
            }

    }











    public void addCannotSitWith(List<Student> students, Student mainStudent)
    {
        if (mainStudent == null || mainStudent.getDbID() <= 0 || students == null || students.isEmpty())
        {
            return;
        }

        String sql = """
            INSERT INTO cannot_sit_with (student_id, cannot_sit_with_student_id)
            VALUES (?, ?)
            """;

        int mainStudentId = mainStudent.getDbID();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Student student : students)
            {
                int std = student.getDbID();
                if (std <= 0)
                {
                    continue;
                }

                statement.setInt(1, mainStudentId);
                statement.setInt(2, std);

                statement.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Could not add cannot sit with relationship");
            e.printStackTrace();
        }

    }









    public void deleteStudent(int stdID)
    {
        String sql = """
            DELETE FROM students
            WHERE  student_id = ? 
            """;




        // Database operations can take time because they involve
        // communication with an external database server.
        // Therefore, the operation is moved to a separate thread
        // so that the JavaFX Application Thread is not blocked.


        SeprateTask.getInstance().offload(()->
        {

            // Opens a database connection and creates a PreparedStatement.
            // The try-with-resources statement automatically closes both
            // resources when they are no longer needed.


            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                    statement.setInt(1, stdID);
                    statement.executeUpdate();






            } catch (SQLException e) {

                // Handles errors that occur while establishing the
                // database connection or creating the PreparedStatement.
                System.out.println("Could not remove Student");
                e.printStackTrace();
            }
        });
    }


    public void deleteAllStudents(int user_id)
    {
        String sql = """
        DELETE FROM students
        WHERE user_id = ?
        """;

        SeprateTask.getInstance().offload(() ->
        {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql))
            {
                statement.setInt(1, user_id);

                int deletedRows = statement.executeUpdate();

                System.out.println("Deleted " + deletedRows + " students");

            }
            catch (SQLException e)
            {
                System.out.println("Could not delete all students");
                e.printStackTrace();
            }
        });
    }








    public boolean isTherePreviousSession(int user_id) {
        String sql = """
                SELECT COUNT(*) FROM students
                WHERE  user_id = ?
                """;

        try (Connection connection = Database.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, user_id);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next())
            {
                int count = resultSet.getInt(1);
                if(count > 0)
                {
                    return true;
                }
                else{
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        catch(SQLException e)
        {
            System.out.println("Could not check if there is a previous session");
            e.printStackTrace();
            return false;

        }
    }

    public void loadPreviousSession(int user_id, VBox studentVBox)
    {
        String sql = """
            SELECT student_id, name, age, class_level, division,
                   sex, seating_preference, img_path
            FROM students
            WHERE user_id = ?
            ORDER BY student_id
            """;

        List<Student> loadedStudents = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, user_id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                String imagePath = resultSet.getString("img_path");

                Image image = null;
                System.out.println(imagePath);

                if (imagePath != null &&
                        !imagePath.equalsIgnoreCase("EMPTY") &&
                        !imagePath.isBlank())
                {
                    try
                    {
                        URI imageURI = URI.create(imagePath);
                        File imageFile = new File(imageURI);

                        if (imageFile.exists())
                        {
                            image = new Image(imageURI.toString());
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        System.out.println("Invalid image path: " + imagePath);
                    }
                }

                Student student = new Student(
                        resultSet.getString("name"),
                        String.valueOf(resultSet.getInt("age")).equals("-1")?"EMPTY":String.valueOf(resultSet.getInt("age")),
                        resultSet.getString("class_level"),
                        resultSet.getString("division"),
                        resultSet.getString("sex"),
                        resultSet.getString("seating_preference"),
                        image,
                        new ArrayList<>()
                );

                // Restore the database ID.
                student.setDbID(
                        resultSet.getInt("student_id")
                );

                loadedStudents.add(student);
            }

        }
        catch (SQLException e)
        {
            System.out.println("Could not load previous session");
            e.printStackTrace();
            return;
        }


        /*
         * ============================================================
         * LOAD CANNOT-SIT-WITH RELATIONSHIPS
         * ============================================================
         */

        String relationshipSQL = """
            SELECT student_id, cannot_sit_with_student_id
            FROM cannot_sit_with
            WHERE student_id IN (
                SELECT student_id
                FROM students
                WHERE user_id = ?
            )
            """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(relationshipSQL))
        {
            statement.setInt(1, user_id);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                int mainStudentID =
                        resultSet.getInt("student_id");

                int cannotSitWithID =
                        resultSet.getInt("cannot_sit_with_student_id");

                Student mainStudent = null;
                Student cannotSitWithStudent = null;

                for (Student student : loadedStudents)
                {
                    if (student.getDbID() == mainStudentID)
                    {
                        mainStudent = student;
                    }

                    if (student.getDbID() == cannotSitWithID)
                    {
                        cannotSitWithStudent = student;
                    }
                }

                if (mainStudent != null &&
                        cannotSitWithStudent != null)
                {
                    mainStudent.getCannotSitWith()
                            .add(cannotSitWithStudent);
                }
            }

        }
        catch (SQLException e)
        {
            System.out.println(
                    "Could not load cannot-sit-with relationships"
            );

            e.printStackTrace();
        }


        /*
         * ============================================================
         * CREATE THE CARDS
         * ============================================================
         */

        for (Student student : loadedStudents)
        {
            Cards.getInstance().createCard(
                    student,
                    studentVBox,
                    false
            );
        }

        /*
         * ============================================================
         * POPULATE CANNOT-SIT-WITH MENU
         *
         * After restoring, we must add a CheckMenuItem for every
         * loaded student into the seprationMenu so that the
         * "Cannot Sit With" MenuButton in the Add Student pane
         * reflects the restored student list.
         * ============================================================
         */

        Platform.runLater(() ->
        {
            for (Student student : loadedStudents)
            {
                CheckMenu.getInstance().createCheckMenuItem(
                        student,
                        MainPage.getInstance().getSeprationMenu()
                );
            }
        });

        System.out.println(
                "Loaded " + loadedStudents.size() +
                        " students from previous session."
        );

        MainPage.getInstance().getGuestNumLabel().setText(String.valueOf(loadedStudents.size()));
    }





    public int getUserId(String email, String userName)
    {
        String sql = """
                SELECT user_id FROM Users
                WHERE email = ? AND username = ?
                """;

        try(Connection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(sql))
        {
            statement.setString(1, email);
            statement.setString(2, userName);

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next())
                return resultSet.getInt("user_id");
            else
                return -1;
        }
        catch(Exception ex)
        {
            System.out.println("Could not get User ID");
            ex.printStackTrace();
            return -1;
        }

    }






}