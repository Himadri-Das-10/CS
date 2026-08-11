package Main;

import SceneManager.SceneManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        stage.setTitle("Home");
        stage.setResizable(false);


        SceneManager.getInstance().setMainStage(stage);
        SceneManager.getInstance().changeScene("home");
        stage.show();


        //Todo
        /*
        -RIGHT CLICK EDITABLE DETAILS
         */

    }
}
