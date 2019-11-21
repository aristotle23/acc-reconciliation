package fitz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class NewAppTabController {

    @FXML
    public TableView<TableProperty> tbl_cb;

    public ObservableList<TableProperty> cbCollection = FXCollections.observableArrayList();

    @FXML
    public TableColumn<TableProperty, String> tbl_cb_date;

    @FXML
    private TableColumn<TableProperty, String> tbl_cb_desc;

    @FXML
    private TableColumn<TableProperty, String> tbl_cb_ref;

    @FXML
    private TableColumn<TableProperty, String> tbl_cb_cr;

    @FXML
    private TableColumn<TableProperty, String> tbl_cb_dr;

    @FXML
    public TableView<TableProperty> tbl_bk;

    public ObservableList<TableProperty> bkCollection = FXCollections.observableArrayList();

    @FXML
    public TableColumn<TableProperty, String> tbl_bk_date;

    @FXML
    private TableColumn<TableProperty, String> tbl_bk_desc;

    @FXML
    private TableColumn<TableProperty, String> tbl_bk_ref;

    @FXML
    private TableColumn<TableProperty, String> tbl_bk_dr;

    @FXML
    private TableColumn<TableProperty, String> tbl_bk_cr;

    public void initialize(){
        tbl_cb.setItems(cbCollection);
        tbl_cb_date.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("date"));
        tbl_cb_desc.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("detail"));
        tbl_cb_dr.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("debit"));
        tbl_cb_cr.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("credit"));
        tbl_cb_ref.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("ref"));

        tbl_bk.setItems(bkCollection);
        tbl_bk_date.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("date"));
        tbl_bk_desc.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("detail"));
        tbl_bk_dr.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("debit"));
        tbl_bk_cr.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("credit"));
        tbl_bk_ref.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("ref"));

        tbl_cb.setRowFactory(tv -> new TableRow<TableProperty>() {
            @Override
            protected void updateItem(TableProperty item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    if (item.getColor().equals(Color.WHITE.toString())){
                        setStyle(String.format("-fx-background-color: %s;", Color.WHITE.toString()));
                    }else
                    if (item.getColor().equals(Color.RED.toString())) {
                        setStyle(String.format("-fx-background-color: %s;", Color.RED.toString()));
                    }else  if(item.getColor().equals(Color.PINK.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.PINK.toString()));
                    }
                }
            }
        });
        tbl_bk.setRowFactory(tv -> new TableRow<TableProperty>() {
            @Override
            protected void updateItem(TableProperty item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    if (item.getColor().equals(Color.WHITE.toString())){
                        setStyle(String.format("-fx-background-color: %s;", Color.WHITE.toString()));
                    }else if (item.getColor().equals(Color.RED.toString())) {
                        setStyle(String.format("-fx-background-color: %s;", Color.RED.toString()));
                    }else if(item.getColor().equals(Color.BLUE.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.BLUE.toString()));
                    }else if(item.getColor().equals(Color.GREEN.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.GREEN.toString()));
                    }
                }
            }
        });
    }
}
