package Controller;

import Backend.Backend;
import CODES.CODES;
import SceneManager.SceneManager;
import Validation.Validation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class Home {

    @FXML
    private TextField emailTF;

    @FXML
    private Pane paneEmail;

    public Home()
    {
        home = this;
    }

    @FXML
    private Pane paneUsername;

    @FXML
    private Button enterBtn;

    @FXML
    private Button retryBtn;

    @FXML
    private Button retryBtn1;

    @FXML
    private Button signupBtn;

    @FXML
    private TextField usernameTF;


    boolean skip = false; //Used for testing and skipping the login part everytime opening a new instance


    private static Home home;



    @FXML
    void enterBtnClicked(ActionEvent event)
    {
        //For a successful entry, both the email and username should be correct

        String email = emailTF.getText().strip();
        String username = usernameTF.getText().strip();

        if(!Validation.getInstance().isValidEmail(email))
        {
            emailTF.clear();
            emailTF.setPromptText("Invalid Email");
            return;
        }

        CODES cd = skip? CODES.SUCCESS: Backend.getInstance().checkUser(email, username);

        switch(cd)
        {
            case NAMEERR ->{   paneUsername.setVisible(true); }
            case EMAILDNE ->{   paneEmail.setVisible(true); }
            case SUCCESS ->
            {

                SceneManager.getInstance().changeScene("otp");
                Otp.getInstance().setLogin(true);
            }
        }


    }

    @FXML
    void retryBtnClicked(ActionEvent event)
    {
        paneEmail.setVisible(false);
        paneUsername.setVisible(false);
        enterBtn.setDisable(false);
        emailTF.setDisable(false);
        usernameTF.setDisable(false);
    }

    @FXML
    void signupBtnClicked(ActionEvent event)
    {
        //For a successful signup, the otp entered should be correct
        SceneManager.getInstance().changeScene("otp");

    }







    //Getters
    public static Home getInstance()
    {
        return home;
    }

    public String getEmail()
    {
        return emailTF.getText().strip();
    }

    public String getUsername()
    {
        return usernameTF.getText().strip();
    }











}
