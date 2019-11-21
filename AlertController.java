package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AlertController {

    @FXML
    private Label lbl_title;

    @FXML
    private Label lbl_content;

    @FXML
    private Button btn_ok;

    @FXML
    void close(ActionEvent event) {
        Stage stage =  (Stage) btn_ok.getScene().getWindow();
        stage.close();
    }

    public Label getLbl_title() {
        return lbl_title;
    }

    public Label getLbl_content() {
        return lbl_content;
    }
}
