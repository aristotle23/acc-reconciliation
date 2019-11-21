package fitz;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DetailSummaryController {

    @FXML
    private Label lbl_balcb;

    @FXML
    private TableView<summaryProperty> tbl_add;

    @FXML
    private TableColumn<summaryProperty, String> col_add_dtl;

    @FXML
    private TableColumn<summaryProperty, Double> col_add_amt;

    @FXML
    private TableView<summaryProperty> tbl_less;

    @FXML
    private TableColumn<summaryProperty, String> col_less_dtl;

    @FXML
    private TableColumn<summaryProperty, Double> col_less_amt;

    @FXML
    private Label lbl_balbk;

    @FXML
    private Label lbl_recbnk;

    @FXML
    private Label lbl_diff;

    private int accId;
    private String from;
    private String to;
    private DatabaseHandler db;
    private double ttlAdd = 0.0;
    private double ttlLess = 0.0;
    @FXML
    void initialize() throws SQLException {
        ResultSet cshBfwdLi = db.getAll("SELECT * FROM csh_book where date between ? and ? and account_id = ? order by id desc limit 1;", new Object[]{from,to,accId});
        cshBfwdLi.next();
        Double cshBfwd = cshBfwdLi.getDouble("balance");
        ResultSet bnkBfwdLi = db.getAll("SELECT * FROM bnk_statement where date between ? and ? and account_id = ? order by id desc limit 1;", new Object[]{from,to,accId});
        bnkBfwdLi.next();
        Double bnkBfwd = bnkBfwdLi.getDouble("balance");


        Double diff = cshBfwd - bnkBfwd;

        lbl_balcb.setText(String.format("%.2f",cshBfwd));
        lbl_balbk.setText(String.format("%.2f",bnkBfwd));
        lbl_diff.setText(String.format("%.2f",bnkBfwd));

        generateAddTbl();
        generateLessTbl();

        Double recbnk = ttlAdd - ttlLess;
        lbl_recbnk.setText(String.format("%.2f",recbnk));

    }
    DetailSummaryController(int accId , String from, String to){
        this.from = from;
        this.to = to;
        this.accId = accId;
        this.db = new DatabaseHandler();
    }
    private void generateAddTbl() throws SQLException {

        ResultSet addLi = db.getAll("SELECT * FROM bnk_statement WHERE credit != 0 and color in (?,?)  and account_id = ? and date between ? and ?",
                new Object[]{Color.GREEN.toString(), Color.RED.toString(),accId, from, to});
        while (addLi.next()){
            String detail = addLi.getString("raw_detail");
            double debit = addLi.getDouble("debit");
            double credit = addLi.getDouble("credit");
            ttlAdd += credit;
            String color = addLi.getString("color");
            tbl_add.getItems().add(new summaryProperty(detail,debit,credit,color));
        }
        addLi = db.getAll("SELECT * FROM csh_book WHERE credit != 0 and color = ?  and account_id = ? and date between ? and ?",
                new Object[]{Color.PINK.toString(), accId, from, to});
        while (addLi.next()){
            String detail = addLi.getString("raw_detail");
            double debit = addLi.getDouble("debit");
            double credit = addLi.getDouble("credit");
            ttlAdd += credit;
            String color = addLi.getString("color");
            tbl_add.getItems().add(new summaryProperty(detail,debit,credit,color));
        }

        col_add_dtl.setCellValueFactory(new PropertyValueFactory<summaryProperty, String>("detail"));
        col_add_amt.setCellValueFactory(new PropertyValueFactory<summaryProperty, Double>("credit"));

        tbl_add.setRowFactory(tv -> new TableRow<summaryProperty>() {
            @Override
            protected void updateItem(summaryProperty item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    if (item.getColor().equals(Color.GREEN.toString())) {
                        setStyle(String.format("-fx-background-color: %s;", Color.GREEN.toString()));
                    }else  if(item.getColor().equals(Color.PINK.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.PINK.toString()));
                    }else  if(item.getColor().equals(Color.RED.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.RED.toString()));
                    }
                }
            }
        });


    }
    private void generateLessTbl() throws SQLException {
        ResultSet lessLi = db.getAll("SELECT * FROM csh_book WHERE debit != 0 and color in (?,?)  and account_id = ? and date between ? and ?",
                new Object[]{Color.PINK.toString(), Color.RED.toString(),accId, from, to});
        while (lessLi.next()){
            String detail = lessLi.getString("raw_detail");
            double debit = lessLi.getDouble("debit");
            double credit = lessLi.getDouble("credit");
            String color = lessLi.getString("color");
            ttlLess += debit;
            tbl_less.getItems().add(new summaryProperty(detail,debit,credit,color));
        }
        lessLi = db.getAll("SELECT * FROM bnk_statement WHERE debit != 0 and color = ?  and account_id = ? and date between ? and ?",
                new Object[]{Color.BLUE.toString(), accId, from, to});
        while (lessLi.next()){
            String detail = lessLi.getString("raw_detail");
            double debit = lessLi.getDouble("debit");
            double credit = lessLi.getDouble("credit");
            String color = lessLi.getString("color");
            ttlLess += debit;
            tbl_less.getItems().add(new summaryProperty(detail,debit,credit,color));
        }
        col_less_dtl.setCellValueFactory(new PropertyValueFactory<summaryProperty, String>("detail"));
        col_less_amt.setCellValueFactory(new PropertyValueFactory<summaryProperty, Double>("debit"));

        tbl_less.setRowFactory(tv -> new TableRow<summaryProperty>() {
            @Override
            protected void updateItem(summaryProperty item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    if (item.getColor().equals(Color.BLUE.toString())) {
                        setStyle(String.format("-fx-background-color: %s;", Color.BLUE.toString()));
                    }else  if(item.getColor().equals(Color.PINK.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.PINK.toString()));
                    }else  if(item.getColor().equals(Color.RED.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.RED.toString()));
                    }
                }
            }
        });
    }

}
