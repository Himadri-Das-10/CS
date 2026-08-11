package UI_Element;

import Data.Student;
import javafx.application.Platform;
import javafx.scene.control.MenuButton;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class CheckMenu {

    private static CheckMenu checkMenuItem;

    private CheckMenu() {}

    public static CheckMenu getInstance() {
        if (checkMenuItem == null) {
            checkMenuItem = new CheckMenu();
        }
        return checkMenuItem;
    }

    public void createCheckMenuItem(
            Student student,
            MenuButton menuButton)
    {

        CheckMenuItem checkMenuItem =
                new CheckMenuItem(student.getName());

        checkMenuItem.setUserData(student);

        Platform.runLater(()->menuButton.getItems().add(checkMenuItem));

    }

    public List<Student> getSelectedStudents(MenuButton menuButton)
    {
        List<Student> selectedStudents = new ArrayList<>();

        for (MenuItem item : menuButton.getItems())
        {
            if (item instanceof CheckMenuItem checkMenuItem
                    && checkMenuItem.isSelected())
            {
                Student student =
                        (Student) checkMenuItem.getUserData();

                selectedStudents.add(student);
            }
        }

        return selectedStudents;
    }


    public void removeCheckMenuItem(
            Student student,
            MenuButton menuButton)
    {
        Platform.runLater(() ->
        {
            menuButton.getItems().removeIf(item ->
                    item instanceof CheckMenuItem checkMenuItem
                            && checkMenuItem.getUserData() == student
            );

            for (MenuItem item : menuButton.getItems())
            {
                if (item instanceof CheckMenuItem checkMenuItem)
                {
                    checkMenuItem.setSelected(false);
                }
            }
        });
    }
}