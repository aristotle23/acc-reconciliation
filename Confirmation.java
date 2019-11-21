package fitz;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Confirmation {
    public static boolean show(String title, String content) throws IOException {
        FXMLLoader loader = new FXMLLoader(Confirmation.class.getResource("scenes/confirmDialog.fxml"));
        HBox root = loader.load();
        ConfirmDialogController controller = loader.getController();
        Label lbl_content = controller.getLbl_content();
        lbl_content.setText(content);
        Label lbl_title = controller.getLbl_title();
        lbl_title.setText(title);


        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();


        return controller.confirmed;
    }
}
