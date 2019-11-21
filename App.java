package fitz;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    MainController main;

    @Override
    public void start(Stage stage) throws IOException, SQLException, SchedulerException {
        int state = 1;
        Connection embededDb = Quartz.conEmbededDb();
        Statement statement = embededDb.createStatement();
        ResultSet chckApp = statement.executeQuery("select * from fitz");
        while (chckApp.next()){
            state = chckApp.getInt("state");
        }
        if(state == 0){
            Alert.show("error","Software Expiration","Fitz has reached its expiration duration. \n" +
                    "Please contact the software manufacturer");
            System.exit(0);
        }
        Quartz.run();

        FXMLLoader loader = new FXMLLoader(App.class.getResource("scenes/main.fxml"));
        stage.setScene(new Scene(loader.load()));

        main = loader.getController();
        stage.setOnCloseRequest(e ->{
            if(main.allThread.size() > 0) {
                for (Thread th : main.allThread
                ) {
                    th.stop();
                }
            }
            System.exit(0);
        });
        stage.getIcons().add(new Image(App.class.getResourceAsStream("icons/window.png")));
        stage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}