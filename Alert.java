package fitz;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

class Alert {
    /**
     *
     * @param type the tpe of the alert - value is either error or anything not error
     * @param title The tile of the error to display
     * @param content The content of error to display - explanation of the error
     * @return void
     * @throws IOException if it couldn't load it throws IOException
     */
    static  void show(String type, String title,String content) throws IOException {


        String sceneType = (type.toLowerCase().equals("error")) ? "scenes/errorAlert.fxml" : "scenes/successAlert.fxml";
        FXMLLoader loader = new FXMLLoader(Alert.class.getResource(sceneType));
        HBox root = loader.load();
        AlertController controller = loader.getController();
        Label lbl_content = controller.getLbl_content();
        lbl_content.setText(content);
        Label lbl_title = controller.getLbl_title();
        lbl_title.setText(title);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(new Scene(root));
        stage.showAndWait();

    }
}
