package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDialogController {

    @FXML
    private Label lbl_title;

    @FXML
    private Label lbl_content;

    @FXML
    private Button btn_yes;

    @FXML
    private Button btn_no;

    boolean confirmed = false;

    @FXML
    void noClicked(ActionEvent event) {
        Stage stage = (Stage) btn_no.getScene().getWindow();
        stage.close();
    }

    @FXML
    void yesClicked(ActionEvent event) {
        confirmed = true;
        Stage stage = (Stage) btn_no.getScene().getWindow();
        stage.close();
    }

    public Label getLbl_content() {
        return lbl_content;
    }

    public Label getLbl_title() {
        return lbl_title;
    }
}
