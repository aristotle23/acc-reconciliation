package fitz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ImportDialogController {

    @FXML
    private ComboBox<AccProperty> cbox_acc;

    @FXML
    public TextField txt_file;

    @FXML
    private Button btn_browse;

    @FXML
    private Button btn_import;

    String importType;

    int accId = 0;

    @FXML
    void  initialize() throws SQLException {
        DatabaseHandler db = new DatabaseHandler();
        ResultSet accDetail = db.getAll("select * from account");
        ObservableList<AccProperty> collections = FXCollections.observableArrayList();
        while ( accDetail.next()){
            String acc_name = accDetail.getString("acc_name");
            String acc_no  = accDetail.getString("acc_no");
            int id = accDetail.getInt("id");


            collections.add(new AccProperty(id,acc_name + " : " + acc_no));


        }
        cbox_acc.setItems(collections);
    }

    @FXML
    void browseCB(ActionEvent event) {
        String fileLoc = fileChooser();
        txt_file.setText(fileLoc);
    }

    @FXML
    void getAccSelected(ActionEvent event){
        AccProperty item = cbox_acc.getSelectionModel().getSelectedItem();
        accId = item.getId();
    }

    @FXML
    void importAccount(ActionEvent event) throws IOException {
        if(txt_file.getText() == "" || accId == 0){
            Alert.show("error", "UNCOMPLETED INFORMATION !!","Import file or account information not selected");
            return;
        }
        Button close = (Button) event.getSource();
        Stage dlgStage = (Stage) close.getScene().getWindow();
        dlgStage.close();

    }
    private String fileChooser(){
        String allFiles = "";
        FileChooser dialog = new FileChooser();
        dialog.setTitle("Select file(s)");
        dialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx"));

        List<File> files = dialog.showOpenMultipleDialog(btn_browse.getScene().getWindow());

        if(files != null) {

            for (int i = 0; i < files.size(); i++) {

                allFiles += files.get(i).getAbsolutePath() + ";";
            }
        }

        return allFiles;
    }

}
