package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NewAccountController {

    @FXML
    private TextField txt_accname;

    @FXML
    private TextField txt_accno;

    @FXML
    private TextField txt_bnkname;

    @FXML
    TextField txt_cb;

    @FXML
    private Button btn_cb;

    @FXML
    TextField txt_bk;

    @FXML
    private Button btn_bk;

    @FXML
    private Button btn_import;

    int accId = 0;

    public MainController parentController;

    @FXML
    void browseBK(ActionEvent event) {
         String fileLoc = fileChooser("Import Bank Statement");
         txt_bk.setText(fileLoc);
    }

    @FXML
    void browseCB(ActionEvent event) {
        String fileLoc = fileChooser("Import Cash Book");
        txt_cb.setText(fileLoc);
    }

    @FXML
    void importAccount(ActionEvent event) throws IOException {
        DatabaseHandler db = new DatabaseHandler();
        if (!validateTxtField(txt_accname) || !validateTxtField(txt_accno) || !validateTxtField(txt_bk)
            || !validateTxtField(txt_bnkname) || !validateTxtField(txt_cb)){
            return;
        }
        Object[] param = {txt_bnkname.getText(),txt_accno.getText(), txt_accname.getText()};
        accId = db.executeGetId("INSERT INTO `account` (`bnk_name`, `acc_no`, `acc_name`) VALUES (?, ?, ?);",param);
        if(accId > 0){
            Stage stage = (Stage) btn_import.getScene().getWindow();
            stage.close();
        }

    }
    private boolean validateTxtField(TextField textField){
        boolean status = true;
        String value = textField.getText();
        if(value.equals("")){
            status = false;
        }
        return status;
    }
    private String fileChooser(String title){
        String allFiles = "";
        FileChooser dialog = new FileChooser();
        dialog.setTitle(title);
        dialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx"));

        List<File> files = dialog.showOpenMultipleDialog(btn_cb.getScene().getWindow());
        if(files != null) {
            for (int i = 0; i < files.size(); i++) {

                allFiles += files.get(i).getAbsolutePath() + ";";
            }
        }

        return allFiles;
    }
    private void newaccountTab(int accId) throws IOException {

        Tab newTab = FXMLLoader.load(getClass().getResource("scenes/newAppTab.fxml"));
        newTab.setText(txt_accname.getText()+":"+ " "+txt_accno.getText());
        parentController.tabPane.getTabs().add(newTab);
        SingleSelectionModel<Tab> model = parentController.tabPane.getSelectionModel();
        model.select(newTab);
        Integer tabIndex = parentController.tabPane.getSelectionModel().getSelectedIndex();
        System.out.println(tabIndex);

        Object[] tabDetail = {accId, newTab};

        parentController.allAccountTab.put(tabIndex,tabDetail);
    }

}
