package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class DateRangeController {

    @FXML
    private DatePicker dp_from;

    @FXML
    private DatePicker dp_to;

    @FXML
    private Button btn_continue;

    private String from = null;
    private String to = null;

    @FXML
    void continueAction(ActionEvent event) {
        from = dp_from.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));;
        to = dp_to.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Stage stage = (Stage) btn_continue.getScene().getWindow();
        stage.close();;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}