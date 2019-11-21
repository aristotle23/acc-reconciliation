package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainController {
    @FXML
    private  Label op_status;
    @FXML
    private MenuItem mi_importBk;

    @FXML
    private MenuItem mi_importCb;

    @FXML
    private MenuItem mi_new;

    @FXML
    private  MenuItem mi_open;

    @FXML
    private Font x3;

    @FXML
    private Color x4;

    @FXML
    public TabPane tabPane;

    @FXML
    private ToggleButton br_btn_white;

    @FXML
    private ToggleGroup colorBtnGroup;

    @FXML
    private ToggleButton br_btn_red;

    @FXML
    private ToggleButton br_btn_pink;

    @FXML
    private ToggleButton br_btn_blue;

    @FXML
    private ToggleButton br_btn_green;

    @FXML
    private Label lbl_newAccount;

    @FXML
    private Label lbl_openAcc;

    @FXML
    private MenuItem mi_safeDelete;

    @FXML
    private ToolBar tbar_acc;

    @FXML
    private  Button btn_reconcile;

    @FXML
    private Button btn_refresh;

    ArrayList<Thread> allThread = new ArrayList<>() ;

    HashMap<Integer, Object[]> allAccountTab; //key = TabIndex Value = {accId, tabController}
    

    public  void initialize(){
        allAccountTab = new HashMap<>();

    }
    /**
     *
     * @param event the event of clicing the new menu
     * @throws IOException
     * open a new account dialog where user can create account and import first cashbook and bankstatemnt record
     * of that account
     */
    @FXML
    void mi_newAccount(ActionEvent event) throws IOException {
        newAccount();
    }
    void newAccount() throws IOException {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("New Account");

        FXMLLoader loader = new FXMLLoader(MainController.class.getResource("scenes/newAccount.fxml"));
        Parent load = loader.load();

        NewAccountController newAccController = loader.getController();
        newAccController.parentController = this;

        stage.setScene(new Scene(load));
        stage.showAndWait();

        if(newAccController.accId == 0 || newAccController.txt_bk.getText().equals("")
                || newAccController.txt_cb.getText().equals("")) return;
        initiateImport(newAccController.txt_bk.getText(),"bnk_statement",newAccController.accId);
        initiateImport(newAccController.txt_cb.getText(),"csh_book",newAccController.accId);
    }
    @FXML
    void lbl_newAccount(MouseEvent event) throws IOException {
        newAccount();
    }

    @FXML
    void lbl_openAccount(MouseEvent event) throws IOException, InterruptedException {
        openAccountDialog();

    }
    @FXML
    void mi_openAccount(ActionEvent event) throws IOException, InterruptedException {
        openAccountDialog();

    }
    void  openAccountDialog() throws IOException, InterruptedException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Open Account");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/openAccount.fxml"));
        Parent load = loader.load();

        openAccountController openaccController = loader.getController();

        stage.setScene(new Scene(load));
        stage.setOnCloseRequest(e -> {
            openaccController.accId = 0;
            openaccController.tabName = null;
        });
        stage.showAndWait();
        if(openaccController.accId == 0) return;
        for (int key: allAccountTab.keySet()
        ) {
            int accId  = (int) allAccountTab.get(key)[0];

            if(accId == openaccController.accId){
                SingleSelectionModel<Tab> tab =tabPane.getSelectionModel();
                tab.select(key);
                return;
            }
        }
        if(newTab(openaccController.accId,openaccController.tabName)){
            Integer tabIndex = tabPane.getSelectionModel().getSelectedIndex();
            Object[] tabDetail = allAccountTab.get(tabIndex);
            int tabAccId = (int) tabDetail[0];
            NewAppTabController tabController = (NewAppTabController) tabDetail[1];

            Reconcile reconcile = new Reconcile(openaccController.accId,tabController,0);
            /*reconcile.setOnSucceeded(e -> {
                GenerateTable bnkTable = new GenerateTable(tabAccId,tabController,"bnk_statement");
                Thread bnkTh = new Thread(bnkTable);
                bnkTh.start();

                GenerateTable cshTable = new GenerateTable(tabAccId,tabController,"csh_book");
                Thread cshTh = new Thread(cshTable);
                cshTh.start();
                allAccountTab.get(tabIndex)[3] = cshTh;


                allAccountTab.get(tabIndex)[4] = bnkTh;
                allThread.add(cshTh);
                allThread.add(bnkTh);

            });*/
            Thread th = new Thread(reconcile);
            th.start();
            //th.join();

            /*GenerateTable bnkTable = new GenerateTable(tabAccId,tabController,"bnk_statement");
            GenerateTable cshTable = new GenerateTable(tabAccId,tabController,"csh_book");
            try {
                cshTable.call();
                bnkTable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }*/


            allAccountTab.get(tabIndex)[2] = th;
            allThread.add(th);

        }

    }
    @FXML
    void safeDelete(ActionEvent event) throws IOException {
        Object[] currentAcc = getCurrentTab();
        int accId = (int) currentAcc[0];
        DeleteImportController controller = new DeleteImportController(accId);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/deleteImport.fxml"));
        loader.setController(controller);
        VBox layout = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Delete Import safely");
        stage.setScene(new Scene(layout));
        stage.showAndWait();

    }
    @FXML
    void importBk(ActionEvent event) throws IOException {
        String[] type = {"bnk_statement","Import Bank Statement"};
        importRecord(type);
    }
    @FXML
    void  importCb(ActionEvent event) throws IOException{
        String[] type = {"csh_book","Import Cash Book"};
        importRecord(type);

    }
    void importRecord(String[] type) throws IOException {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(type[1]);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/import.fxml"));
        VBox importScene = loader.load();
        ImportDialogController iDlg = loader.getController();
        iDlg.importType = type[0];

        stage.setScene(new Scene(importScene));
        stage.showAndWait();
        if(iDlg.accId == 0 || iDlg.txt_file.getText().equals("")) return;
        initiateImport(iDlg.txt_file.getText(),iDlg.importType,iDlg.accId);
    }
    private void  initiateImport(String files, String importType, int accId) throws IOException {
        String title = (importType.equals("csh_book")) ? "Cash book" : "Bank statement";

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/progressBar.fxml"));
        Parent bar = loader.load();

        ProgressBarController barController = loader.getController();


        ImportRecord importTask = new ImportRecord(files,importType,accId,barController.lbl_progress,op_status);
        barController.progressbar.progressProperty().bind(importTask.progressProperty());
        barController.progressbar.progressProperty().addListener(
                (observable, oldvalue, newvalue) -> {
                    Double progress = (Double) newvalue;
                    if(progress == 1.0) {

                        op_status.setText(title +" "+"Imported successfully" );
                        stage.close();
                    }
                }
                // code goes here
        );

        Thread th = new Thread(importTask);
        th.start();


        stage.setScene(new Scene(bar));

        stage.showAndWait();

    }

    /**
     *
     * @param accId The accound Id of the import recod
     * @param accname the account name of the import record
     * @throws IOException
     * Create a new tab for new account recounciliation
     * It loads the tab from initialized tab fxml file;
     */
    private boolean newTab (int accId, String accname) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/newAppTab.fxml"));
        Tab newTab = loader.load();

        newTab.setText(accname);
        tabPane.getTabs().add(newTab);

        NewAppTabController tabController = loader.getController();

        SingleSelectionModel<Tab> model = tabPane.getSelectionModel();
        model.select(newTab);
        Integer tabIndex = tabPane.getSelectionModel().getSelectedIndex();

        newTab.setOnCloseRequest(e -> {
            Object[] tabDetail = allAccountTab.get(tabIndex);

            allAccountTab.remove(tabIndex);
            if(allAccountTab.size() < 1 ){
                mi_safeDelete.setDisable(true);
                tbar_acc.setDisable(true);
            }

        });

        //Object[] tabDetail = {accId, tabController};
        Object[] tabDetail = new Object[3];
        tabDetail[0] = accId;
        tabDetail[1] = tabController;

        allAccountTab.put(tabIndex,tabDetail);
        tbar_acc.setDisable(false);
        mi_safeDelete.setDisable(false);

        return  true;
    }
    /**
     * get the current selected tab
     * @return current selected tab accId and controller. both information are in list = {accId, Controller}
     */
    Object[] getCurrentTab(){
        if(allAccountTab.size() < 1) return  null;
        int tabIndex = tabPane.getSelectionModel().getSelectedIndex();
        return  allAccountTab.get(tabIndex);
    }
    String[] getDateRange() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/dateRange.fxml"));
        VBox root = loader.load();

        DateRangeController controller = loader.getController();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();

        if(controller.getFrom() == null || controller.getTo() == null) return null;
        return new String[]{controller.getFrom(), controller.getTo()};
    }
    @FXML
    void colorInfo(ActionEvent event) throws IOException, SQLException {
        if(getCurrentTab() == null) return;
        ToggleButton button = (ToggleButton) event.getSource();
        String type = button.getId();

        if(type.equals("br_btn_white") || type.equals("br_btn_red")){
            String choice = exportChoice();
            if(choice == null) return;
            type += "_"+choice;
        }

        String[] dateRange = getDateRange();
        String from = dateRange[0];
        String to = dateRange[1];

        ExportController controller = new ExportController(type,getCurrentTab(),from,to);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/export.fxml"));
        loader.setController(controller);
        VBox root = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));

        stage.setOnCloseRequest(e->{
            button.setSelected(false);
        });
        stage.showAndWait();
        //if(controller.getResultLi() == null) return;
        //initiateExport(controller.getResultLi(),controller.getFile());
    }
    String exportChoice() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/exportChoice.fxml"));
        HBox root = loader.load();
        ExportChoiceController controller = loader.getController();

        Stage stage = new Stage();
        stage.setTitle("Record Type");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));


        stage.showAndWait();
        String choice = controller.getChoice();
        return  choice;

    }
    @FXML
    void  reconcileClicked(ActionEvent event) throws IOException {
        Object[] currentTab =  getCurrentTab();
        int accId = (int) currentTab[0];
        String[] dateRange = getDateRange();
        if(dateRange == null)return;
        String from = dateRange[0];
        String to = dateRange[1];

        DetailSummaryController detailSummary = new DetailSummaryController(accId,from,to);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/detailSummary.fxml"));
        loader.setController(detailSummary);
        VBox root = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Reconciled Statement");
        stage.setScene(new Scene(root));
        stage.showAndWait();


    }
    @FXML
    void refreshClicked(ActionEvent event){
        Object[] currentTab = getCurrentTab();
        int accId = (int) currentTab[0];
        NewAppTabController tabController = (NewAppTabController) currentTab[1];

        Reconcile reconcile = new Reconcile(accId,tabController,1);
        Thread th = new Thread(reconcile);
        th.start();

    }

}


