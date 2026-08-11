package Controller;

import Backend.Backend;

import Data.*;

import Offload.SeprateTask;
import SceneManager.SceneManager;
import UI_Element.Cards;
import UI_Element.CheckMenu;
import Validation.Validation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import CODES.CODES;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainPage {

    @FXML
    private Button addBtn;

    @FXML
    private Button addFromCsvBtn;

    @FXML
    private Pane addStudentPane;

    @FXML
    private TextField ageTF;

    @FXML
    private Button finishBtn;


    @FXML
    private Pane errorPane;

    @FXML
    private Text errorText;

    @FXML
    private AnchorPane root;

    @FXML
    private Button okBtn;

    @FXML
    private Button allotStudentsBtn;

    @FXML
    private MenuButton classMenu;

    @FXML
    private MenuButton divisionMenu;

    @FXML
    private MenuButton seatingMenu;

    @FXML
    private Button exportToPDFBtn;

    @FXML
    private GridPane seatingPlan;

    @FXML
    private Label guestNumLabel;

    @FXML
    private TextField guestNumTF;

    @FXML
    private Button imageBtn;

    @FXML
    private TextField nameTF;

    @FXML
    private TextField studentSearch;

    @FXML
    private MenuButton personalizeMenu;

    @FXML
    private MenuButton seprationMenu;

    @FXML
    private MenuButton settingMenu;

    @FXML
    private MenuButton sexMenu;

    @FXML
    private ImageView studentImage;

    @FXML
    private VBox studentVBox;

    @FXML
    private TextField usernameTF;




    private static MainPage mainPage;
    public MainPage()
    {
        mainPage = this;
    }
    public static MainPage getInstance()
    {
        return mainPage;
    }





    @FXML
    public void  initialize()
    {

        usernameTF.setText(Home.getInstance().getUsername());
        usernameTF.setEditable(false);


        studentSearch.textProperty().addListener((observable, oldValue, newValue) ->
        {
            Platform.runLater(()->{

                List<Student> std = SearchStudents.studentSearch(newValue);

                studentVBox.getChildren().clear();
                std.forEach(student -> Cards.getInstance().createCard(student, studentVBox));
            });
        });






    }

    @FXML
    void guestNumTFclicked(MouseEvent event)
    {
        usernameTF.setEditable(!usernameTF.isEditable());
    }

    @FXML
    void guestNumTFpressed(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER)
        {
            try{
                int seatNumbers =  Integer.parseInt(guestNumTF.getText());
                guestNumTF.setEditable(false);
            }
            catch(NumberFormatException ex)
            {
                guestNumTF.setText("Please enter a whole number");
                return;
            }

        }
    }

    @FXML
    void usernameTFclicked(MouseEvent event)
    {
        usernameTF.setEditable(!usernameTF.isEditable());
    }

    @FXML
    void usernameTFpressed(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER)
        {
            usernameTF.setEditable(false);
            Backend.getInstance().changeUserName(
                    usernameTF.getText().strip(),
                    Home.getInstance().getEmail()
            );
        }
    }









    @FXML
    void finishBtnClicked(ActionEvent event)
    {
        if (nameTF.getText().isBlank())
        {
            nameTF.clear();
            nameTF.setPromptText("Name is mandatory");
            return;
        }

        String name = nameTF.getText().strip();

        String age;

        if (ageTF.getText().isBlank())
        {
            age = String.valueOf(CODES.EMPTY);
        }
        else
        {
            try
            {
                age = String.valueOf(
                        Integer.parseInt(ageTF.getText().strip())
                );
            }
            catch (NumberFormatException e)
            {
                ageTF.clear();
                ageTF.setPromptText("Age should be a number");
                return;
            }
        }

// Get the selected values from the menus.
// If nothing was selected, store CODES.EMPTY.
        String sex = sexMenu.getText().isBlank() || sexMenu.getText().equals("Choose")
                ? String.valueOf(CODES.EMPTY)
                : sexMenu.getText().strip();

        String classLevel = classMenu.getText().isBlank() || classMenu.getText().equals("Choose")
                ? String.valueOf(CODES.EMPTY)
                : classMenu.getText().strip();

        String division = divisionMenu.getText().isBlank() || divisionMenu.getText().equals("Choose")
                ? String.valueOf(CODES.EMPTY)
                : divisionMenu.getText().strip();

        String seatingPreference = settingMenu.getText().isBlank() || seatingMenu.getText().equals("Select")
                ? String.valueOf(CODES.EMPTY)
                : settingMenu.getText().strip();

// Get the selected student image.
        Image img = studentImage.getImage();

        Student newStudent = new Student(
                name,
                age,
                classLevel,
                division,
                sex,
                seatingPreference,
                img,
                CheckMenu.getInstance().getSelectedStudents(seprationMenu)
        );

        Cards.getInstance().createCard(newStudent, studentVBox);






        nameTF.clear();
        ageTF.clear();
        classMenu.setText("Choose");
        divisionMenu.setText("Choose");
        sexMenu.setText("Choose");
        seatingMenu.setText("Select");
        studentImage.setImage(null);

        guestNumLabel.setText(String.valueOf
                (
                Integer.parseInt(
                        guestNumLabel.getText().strip()
                )+1
                )
        );

        CheckMenu.getInstance().createCheckMenuItem(newStudent, seprationMenu);
    }





    @FXML
    void addBtnClicked(ActionEvent event)
    {
        addStudentPane.setVisible(!addStudentPane.isVisible());
    }



    @FXML
    void menuItemSelected(ActionEvent event)
    {
        MenuItem selectedItem = (MenuItem) event.getSource();

        String selectedValue = selectedItem.getText();

        MenuButton menuButton = (MenuButton) selectedItem.getParentPopup().getOwnerNode();

        menuButton.setText(selectedValue);
    }



    @FXML
    void imageBtnClicked(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();

        // Set the title shown in the file explorer
        fileChooser.setTitle("Select Student Image");

        // Only show image files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif"
                )
        );



        // Open the file explorer
        File selectedFile = fileChooser.showOpenDialog(SceneManager.getInstance().getMainStage());

        // If the user selected an image
        if (selectedFile != null)
        {
            // Load the selected image
            Image image = new Image(
                    selectedFile.toURI().toString()
            );

            // Display it in the ImageView
            studentImage.setImage(image);
        }
    }




    @FXML
    void studentSearchClicked(ActionEvent event)
    {
        studentSearch.setEditable(!studentSearch.isEditable());
    }









    @FXML
    void addFromCsvBtnClicked(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();

        // Set the title shown in the file explorer
        fileChooser.setTitle("Select Student Image");

        // Only show csv files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV Files",
                        "*.csv"
                )
        );



        // Open the file explorer
        File selectedFile = fileChooser.showOpenDialog(SceneManager.getInstance().getMainStage());


        if (selectedFile != null)
        {

            if(Validation.getInstance().validateCSV(selectedFile).equals(CODES.INVALID))
            {
                errorText.setText(Validation.errorTemplateInvalidCSVdata);
                errorPane.setVisible(true);
            }
            else
            {
                Cards.getInstance().createCard(selectedFile, studentVBox);
            }


        }
        else
        {
            errorText.setText(Validation.errorTemplateInvalidCSVdata);
            errorText.setVisible(true);
        }
    }


    @FXML
    void okBtnClicked(ActionEvent event)
    {
        errorPane.setVisible(!errorPane.isVisible());
    }


    public Pane getErrorPane()
    {
        return errorPane;
    }

    public Label getGuestNumLabel()
    {
        return guestNumLabel;
    }









    @FXML
    void sepBoysAndGirlsSelected(ActionEvent event)
    {
        SeprateTask.getInstance().offload(()->
        {
            Platform.runLater(()-> {
                studentVBox.getChildren().clear();
                PartStudents.getInstance().partStudents(studentVBox);
            });

        });

        personalizeMenu.setText("Separate Boy and Girls");
    }

    @FXML
    void shuffleStudentsSelected(ActionEvent event)
    {

        SeprateTask.getInstance().offload(()->
        {
            Platform.runLater(()-> {
                studentVBox.getChildren().clear();
                RanShuffleStudent.getInstance().ranShuffleStudent(studentVBox);
            });

        });

        personalizeMenu.setText("Shuffle Students");

    }

    @FXML
    void sortSelected(ActionEvent event)
    {
        SeprateTask.getInstance().offload(()->
        {
            Platform.runLater(()-> {
                studentVBox.getChildren().clear();
                SortStudents.getInstance().sortStudents(studentVBox);
            });

        });

        personalizeMenu.setText("Sort");

    }


    @FXML
    void delAllBtnClicked(ActionEvent event)
    {
        guestNumLabel.setText("0");
        studentVBox.getChildren().clear();
        Student.students.clear();
        seprationMenu.getItems().clear();
    }


    public GridPane getSeatingPlan()
    {
        return seatingPlan;
    }




    @FXML
    void allotStudentsBtnClicked(ActionEvent event)
    {
        AllotStudents.getInstance().generateSeating(Student.students, Integer.parseInt(guestNumTF.getText()));
    }



    public void showEditStudentPane(Student student)
    {
        addStudentPane.setVisible(true);
        addStudentPane.setDisable(false);

        nameTF.setText(student.getName());
        ageTF.setText(student.getAge());

        classMenu.setText(student.getClassLevel());
        divisionMenu.setText(student.getDivision());
        sexMenu.setText(student.getSex());
        seatingMenu.setText(
                student.getSeatingPreference()
        );
        studentImage.setImage(student.getImage());
    }




    public void showStudentDetails(Student student)
    {
        addStudentPane.setVisible(true);

        nameTF.setText(student.getName());
        ageTF.setText(student.getAge());

        classMenu.setText(student.getClassLevel());
        divisionMenu.setText(student.getDivision());
        sexMenu.setText(student.getSex());
        seatingMenu.setText(
                student.getSeatingPreference()
        );
        studentImage.setImage(student.getImage());

        addStudentPane.setDisable(true);
    }



}
