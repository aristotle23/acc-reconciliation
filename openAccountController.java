package fitz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class openAccountController {

    @FXML
    private ComboBox<AccProperty> cbox_acc;

    @FXML
    private Button btn_open;

    int accId = 0;
    String tabName = null;


    @FXML
    void  initialize() throws SQLException {
        DatabaseHandler db = new DatabaseHandler();
        ResultSet accDetail = db.getAll("select * from account");
        ObservableList<AccProperty> collections = FXCollections.observableArrayList();
        while ( accDetail.next()){
            String acc_name = accDetail.getString("acc_name");
            String acc_no  = accDetail.getString("acc_no");
            int id = accDetail.getInt("id");
            tabName = acc_name + " : " + acc_no;

            collections.add(new AccProperty(id,acc_name + " : " + acc_no));


        }
        cbox_acc.setItems(collections);



    }
    public  void closeDialog(){
        accId = 0;
        tabName = null;
        System.out.println("close");
    }
    @FXML
    void getAccSelected(ActionEvent event) {
        AccProperty item = cbox_acc.getSelectionModel().getSelectedItem();
        accId = item.getId();
        tabName = item.getText();
    }

    @FXML
    void openAccount(ActionEvent event) throws IOException {
        if(accId == 0){
            Alert.show("error", "NO SELECTION!!","Please select account you wish to open for reconciliation");
            return;
        }
        Stage stage = (Stage) btn_open.getScene().getWindow();
        stage.close();

    }

}
