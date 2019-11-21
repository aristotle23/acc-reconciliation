package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ExportChoiceController {

    @FXML
    private RadioButton bnk_statement;

    @FXML
    private ToggleGroup rb_group;

    @FXML
    private RadioButton csh_book;

    @FXML
    private RadioButton none;

    @FXML
    private Button btn_continue;

    private String choice = null;

    @FXML
    void continueClicked(ActionEvent event) {
        RadioButton rb = (RadioButton) rb_group.getSelectedToggle();
        choice = ( rb.getId().equals("none") ) ? null : rb.getId();
        Stage stage = (Stage) rb.getScene().getWindow();
        stage.close();
    }

    String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }
}
