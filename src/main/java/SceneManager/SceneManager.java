package SceneManager;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class SceneManager
{
    private static SceneManager sceneManager;
    private Stage mainStage;
    private Stack<Scene> sceneStack = new Stack<>();


    private SceneManager(){}
    public void changeScene(String fileName)
    {
        //All the .fxml files are stored in the 'resource root'.
        //This is a shortcut to access those files
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLfiles/"+fileName+".fxml"));


        try
        {  Parent root = loader.load();
            Scene scene = new Scene(root);
            sceneStack.push(scene);


            // Platform.runLater() schedules the code to execute on the
            // JavaFX Application Thread, allowing the UI to be safely updated.
            Platform.runLater(()->{mainStage.setScene(scene);});


        }
        catch(IOException e){ System.out.println("The scene file could not be loaded"); e.printStackTrace(); }








    }


    public void undoCurrentScene()
    {
        sceneStack.pop();
        mainStage.setScene(sceneStack.peek());
    }










    //Setters--------------------

    public void setMainStage(Stage mainStage)
    {
        this.mainStage = mainStage;
    }











    //Getter--------------------


    public static  SceneManager getInstance()
    {
        if(sceneManager == null)
        {
            sceneManager = new SceneManager();
        }
        return sceneManager;
    }

    public Stage getMainStage()
    {
        if(mainStage == null)
            {
            mainStage = new Stage();
            }
        return mainStage;
    }

}
