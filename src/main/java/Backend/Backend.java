package Backend;

import CODES.CODES;
import Controller.Home;
import Data.Student;
import Offload.SeprateTask;

import java.sql.*;
import java.util.concurrent.atomic.AtomicReference;


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





    public String getUserId(String email) {

        String sql = """
        SELECT user_id
        FROM Users
        WHERE email = ?
        """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Assigns the email to the '?' placeholder.
            statement.setString(1, email);

            // Executes the prepared SQL query.
            ResultSet resp = statement.executeQuery();

            // Moves the ResultSet to the first returned row.
            // If there is no row, the email does not exist.
            if (resp.next()) {

                return resp.getString("user_id");
            }

            // No user was found with this email.
            return null;

        } catch (SQLException e) {

            // Handles database-related errors.
            System.out.println("Could not get userID");
            e.printStackTrace();
            return null;
        }
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

                // Assigns the email to the first '?' placeholder.
                statement.setString(1, email);

                // Assigns the username to the second '?' placeholder.
                statement.setString(2, username);


                // Executes the INSERT query and adds the user
                // to the Users table.
                statement.executeUpdate();


            } catch (SQLException e) {

                // Handles errors that occur while establishing the
                // database connection or creating the PreparedStatement.
                System.out.println("Could not add user");
            }
        });
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





}