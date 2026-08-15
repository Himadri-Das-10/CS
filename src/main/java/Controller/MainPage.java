package Controller;

import Backend.Backend;

import Enums.Setting;
import Features.*;

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
import Enums.CODES;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPage {

    @FXML
    private Button addBtn;

    @FXML
    private Button addFromCsvBtn;

    @FXML
    private Pane addStudentPane;

    @FXML
    private TextField ageDTF;

    @FXML
    private TextField ageTF;

    @FXML
    private Button allotStudentsBtn;

    @FXML
    private MenuButton cannotSitWithD;

    @FXML
    private MenuButton classMenu;

    @FXML
    private GridPane seatGrid;

    @FXML
    private MenuButton classMenuButtonD;

    @FXML
    private Pane detailsPane;

    @FXML
    private Pane restorePane;

    @FXML
    private Pane mainPane;

    @FXML
    private MenuButton divisionMenu;

    @FXML
    private MenuButton divisionMenuBtnD;

    @FXML
    private Button editBtn;

    @FXML
    private Pane errorPane;

    @FXML
    private Text errorText;

    @FXML
    private Button exportToPDFBtn;

    @FXML
    private Button finishBtn;

    @FXML
    private Label guestNumLabel;

    @FXML
    private TextField guestNumTF;

    @FXML
    private Button imageBtn;

    @FXML
    private TextField nameDTF;

    @FXML
    private TextField nameTF;

    @FXML
    private Button okBtn;

    @FXML
    private Button okayBtnD;

    @FXML
    private MenuButton personalizeMenu;

    @FXML
    private AnchorPane root;

    @FXML
    private MenuButton seatingMenu;

    @FXML
    private MenuButton seatingPreferenceD;

    @FXML
    private MenuButton seprationMenu;

    @FXML
    private MenuButton settingMenu;

    @FXML
    private MenuButton sexMenu;

    @FXML
    private MenuButton sexMenuButtonD;

    @FXML
    private ImageView studentImage;

    @FXML
    private ImageView studentImageD;

    @FXML
    private TextField studentSearch;

    @FXML
    private VBox studentVBox;

    @FXML
    private TextField usernameTF;



    private boolean editMode = false;
    private Student editingStudent;







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
            List<Student> std = SearchStudents.studentSearch(newValue);

            studentVBox.getChildren().clear();
            std.forEach(student -> Cards.getInstance().createCard(student, studentVBox, false));
        });


        if(Backend.getInstance().isTherePreviousSession(
                Student.userID
        )
        )
        {
            restorePane.setVisible(true);
            mainPane.setVisible(true);
        }



    }

    @FXML
    void guestNumTFclicked(MouseEvent event)
    {
        guestNumTF.setEditable(!guestNumTF.isEditable());
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
            if(usernameTF.getText().isBlank())
            {
                usernameTF.setText("Username can't be empty");
                usernameTF.setEditable(false);
                return;

            }
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

        // Cleanly map enum values with efficient null checks
        String sex = Enums.EnumMapper.toSexDb(sexMenu.getText());
        String classLevel = Enums.EnumMapper.toClassLevelDb(classMenu.getText());
        String division = Enums.EnumMapper.toDivisionDb(divisionMenu.getText());
        String seatingPreference = Enums.EnumMapper.toSeatingPrefDb(seatingMenu.getText());

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

        Cards.getInstance().createCard(newStudent, studentVBox, true);







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
        for (MenuItem item : seprationMenu.getItems())
        {
            if (item instanceof CheckMenuItem checkMenuItem)
            {
                checkMenuItem.setSelected(false);
            }
        }
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

    @FXML
    void okayBtnDClicked(ActionEvent event)
    {
        if(editMode)
        {
            if (nameDTF.getText().isBlank())
            {
                nameDTF.clear();
                nameDTF.setPromptText("Name is mandatory");
                return;
            }

            String name = nameDTF.getText().strip();

            String age;

            if (ageDTF.getText().isBlank())
            {
                age = String.valueOf(CODES.EMPTY);
            }
            else
            {
                try
                {
                    age = String.valueOf(
                            Integer.parseInt(ageDTF.getText().strip())
                    );
                }
                catch (NumberFormatException e)
                {
                    ageDTF.clear();
                    ageDTF.setPromptText("Age should be a number");
                    return;
                }
            }

            // Cleanly map enum values with efficient null checks
            String sex = Enums.EnumMapper.toSexDb(sexMenuButtonD.getText());
            String classLevel = Enums.EnumMapper.toClassLevelDb(classMenuButtonD.getText());
            String division = Enums.EnumMapper.toDivisionDb(divisionMenuBtnD.getText());
            String seatingPreference = Enums.EnumMapper.toSeatingPrefDb(seatingPreferenceD.getText());

            // Get the selected student image.
            Image img = studentImageD.getImage();

            Student newStudent = new Student(
                    name,
                    age,
                    classLevel,
                    division,
                    sex,
                    seatingPreference,
                    img,
                    CheckMenu.getInstance().getSelectedStudents(cannotSitWithD)
            );



            Student.students.remove(editingStudent);
            Backend.getInstance().deleteStudent(editingStudent.getDbID());
            for(Node nd: studentVBox.getChildren())
            {
                if(nd.getUserData().equals(editingStudent))
                {
                    studentVBox.getChildren().remove(nd);

                    break;
                }
            }

            editingStudent = null;

            Cards.getInstance().createCard(newStudent, studentVBox, true);





            CheckMenu.getInstance().createCheckMenuItem(newStudent, cannotSitWithD);

            detailsPane.setVisible(false);
            editMode = false;
        }
        else
        {
            detailsPane.setVisible(!detailsPane.isVisible());
        }

    }

    @FXML
    void editBtnClicked(ActionEvent event)
    {

        nameDTF.setEditable(true);
        ageDTF.setEditable(true);
        studentImageD.setDisable(false);
        for (MenuItem item : classMenuButtonD.getItems())
        {
            item.setDisable(false);
        }

        for (MenuItem item : divisionMenuBtnD.getItems())
        {
            item.setDisable(false);
        }

        for (MenuItem item : sexMenuButtonD.getItems())
        {
            item.setDisable(false);
        }

        for (MenuItem item : seatingPreferenceD.getItems())
        {
            item.setDisable(false);
        }


        cannotSitWithD.getItems().clear();
        for(Student student : Student.students)
        {
            if(student.equals(editingStudent))
                continue;
            else
                CheckMenu.getInstance().createCheckMenuItem(student, cannotSitWithD);
        }


        studentImageD.setDisable(false);
        editMode = true;
    }


    public Pane getErrorPane()
    {
        return errorPane;
    }

    public Label getGuestNumLabel()
    {
        return guestNumLabel;
    }









    /**
     * Executes a student arrangement algorithm polymorphically via the Strategy Design Pattern.
     *
     * @param strategy the concrete arrangement strategy to execute
     */
    private void executeArrangementStrategy(StudentArrangementStrategy strategy)
    {
        List<Student> arranged = strategy.arrange(Student.students);
        studentVBox.getChildren().clear();
        for (Student student : arranged)
        {
            Cards.getInstance().createCard(student, studentVBox, false);
        }
        personalizeMenu.setText(strategy.getName());
    }

    @FXML
    void sepBoysAndGirlsSelected(ActionEvent event)
    {
        executeArrangementStrategy(new GenderPartitionStrategy());
    }

    @FXML
    void shuffleStudentsSelected(ActionEvent event)
    {
        executeArrangementStrategy(new RandomShuffleStrategy());
    }

    @FXML
    void sortSelected(ActionEvent event)
    {
        executeArrangementStrategy(new AlphabeticalSortStrategy());
    }


    @FXML
    void delAllBtnClicked(ActionEvent event)
    {


        Backend.getInstance().deleteAllStudents(Student.userID);

        Student.students.clear();

        Platform.runLater(() ->
        {
            guestNumLabel.setText("0");
            studentVBox.getChildren().clear();
            seprationMenu.getItems().clear();
        });
    }







    @FXML
    void allotStudentsBtnClicked(ActionEvent event)
    {
        CODES cd = Validation.getInstance().validateIntegerField(errorPane, guestNumTF.getText().strip(), errorText, "SEATS", guestNumLabel.getText().strip());
        if(!cd.toString().equals("INVALID"))
        {
            Setting rtype = Setting.fromString(settingMenu.getText());

            AllotStudents.getInstance().renderSeating(
                    AllotStudents.getInstance().generateSeating(Student.students,
                            Integer.parseInt(guestNumTF.getText().strip()), rtype)
                    , seatGrid);
        }
        else
        {
            // No valid arrangement could be found — every backtracking
            // path failed the cannotSitWith constraints.

            Platform.runLater(()->{errorText.setText(Validation.errorTemplateInvalidSeats);
                errorPane.setVisible(true);});


            System.out.println("Could not generate a valid seating arrangement.");
        }
    }







    public void showStudentDetails(Student student)
    {
        editingStudent = student;
        detailsPane.setVisible(true);

        nameDTF.setText(student.getName());
        ageDTF.setText(student.getAge());

        classMenuButtonD.setText(student.getClassLevel());
        divisionMenuBtnD.setText(student.getDivision());
        sexMenuButtonD.setText(student.getSex());
        seatingPreferenceD.setText(student.getSeatingPreference());
        studentImageD.setImage(student.getImage());

        if (student.getSex().equalsIgnoreCase("Male"))
        {
            detailsPane.setStyle("-fx-background-color: #536DFE;");
        }
        else if (student.getSex().equalsIgnoreCase("Female"))
        {
            detailsPane.setStyle("-fx-background-color: #E8A0BF;");
        }

        nameDTF.setEditable(false);
        ageDTF.setEditable(false);

        for (MenuItem item : classMenuButtonD.getItems())
        {
            item.setDisable(true);
        }
        for (MenuItem item : divisionMenuBtnD.getItems())
        {
            item.setDisable(true);
        }
        for (MenuItem item : sexMenuButtonD.getItems())
        {
            item.setDisable(true);
        }
        for (MenuItem item : seatingPreferenceD.getItems())
        {
            item.setDisable(true);
        }

        // Clear out any leftover checkboxes from a previous
        // Details/Edit session before rebuilding this one.
        cannotSitWithD.getItems().clear();

        for (Student s : student.getCannotSitWith())
        {
            CheckMenu.getInstance().createCheckMenuItem(s, cannotSitWithD);
        }

        // Disable them now that they actually exist — this is a
        // read-only view, so nothing in cannotSitWithD should be
        // interactive until Edit is clicked.
        for (MenuItem item : cannotSitWithD.getItems())
        {
            item.setDisable(true);
        }

        studentImageD.setImage(student.getImage());
        studentImageD.setDisable(true);
    }


    @FXML
    void studentImageDClicked(MouseEvent event)
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
            studentImageD.setImage(image);
        }
    }



    public VBox getStudentVBox()
    {
        return studentVBox;
    }

    public MenuButton getSeprationMenu()
    {
        return seprationMenu;
    }


    @FXML
    void exportToPDFBtnClicked(ActionEvent event) {
        Map<String, String> data = new HashMap<>();
        data.put(PdfExport.KEY_NAME, usernameTF.getText());
        data.put(PdfExport.KEY_SETTING, settingMenu.getText().equals("Setting") ? "Classroom" : settingMenu.getText().strip());
        data.put(PdfExport.KEY_TOTAL_STUDENTS, String.valueOf(Student.students.size()));
        data.put(PdfExport.KEY_TOTAL_SEATS, guestNumTF.getText());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Seating Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File saveFile = fileChooser.showSaveDialog(SceneManager.getInstance().getMainStage());
        if (saveFile != null) {
            // UI node capture and PDF generation executed safely
            PdfExport.getInstance().convertIntoPdf(data, studentVBox, seatGrid, saveFile.getAbsolutePath());
        }
    }



    @FXML
    void restoreOkayBtnClicked(ActionEvent event)
    {
        mainPane.setDisable(false);
        Backend.getInstance().loadPreviousSession(
                Student.userID,
                studentVBox);

        restorePane.setVisible(false);

    }



    @FXML
    void restoreSkipBtnClicked(ActionEvent event)
    {
        restorePane.setVisible(false);
        mainPane.setDisable(false);
    }



}
