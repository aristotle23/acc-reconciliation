package fitz;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

class DeleteImportController {

    @FXML
    private TableView<LogProperty> tbl_delete;

    @FXML
    private TableColumn<LogProperty, String> col_stamp;

    @FXML
    private TableColumn<LogProperty, String> col_line;
    private int accId;
    private DatabaseHandler db;
    DeleteImportController(int accId){
        this.accId = accId;
        this.db = new DatabaseHandler();
    }

    @FXML
    void initialize() throws SQLException {
        ResultSet logLi = db.getAll("select * from import_log where account_id = ?", new Object[]{accId});
        while (logLi.next()){
            String timestamp = logLi.getString("start_time");
            String type = (logLi.getString("type").equals("bnk_statement") ) ? "Bank Statement" : "Cash Book" ;
            String elapse = elapse(logLi.getString("start_time"),logLi.getString("end_time"));
            String timeline = type + " imported successfully in "+ elapse;
            tbl_delete.getItems().add(new LogProperty(timestamp,timeline,logLi.getInt("id")));
        }

        col_line.setCellValueFactory(new PropertyValueFactory<LogProperty,String>("timeline"));
        col_stamp.setCellValueFactory(new PropertyValueFactory<LogProperty,String>("timestamp"));
    }
    @FXML
    void recordSelected(MouseEvent event) throws IOException, SQLException {

        boolean delChoice = Confirmation.show("ARE YOU SURE??","Be certain you selected the right import.. Import deleted cannot be recovered||");
        if(delChoice){
            deleteImport();
        }


    }
    void deleteImport() throws SQLException {
        LogProperty item = tbl_delete.getSelectionModel().getSelectedItem();
        int logId = item.getLogId();

        ResultSet log = db.getAll("select * from import_log where id = ?",new Object[]{logId});
        log.next();
        int start = log.getInt("start");
        int end = log.getInt("end");

        String opposite = (log.getString("type").equals("csh_book")) ? "bnk_statement" : "csh_book";
        String oppositeRefId  =   (log.getString("type").equals("csh_book")) ? "csh_book_id" : "bnk_statement_id";

        String sql = String.format("delete from %s where id >= ? and id <= ? ",log.getString("type"));
        String sql2 = String.format("UPDATE %s SET `ref`= NULL , %s = 0 where %s >= ? and %s <= ? ", opposite,oppositeRefId,oppositeRefId,oppositeRefId );
        db.execute(sql,new Object[]{start,end});
        db.execute("delete from import_log where id = ?",new Object[]{logId});
        db.execute(sql2,new Object[]{start,end});
        tbl_delete.getItems().remove(item);

    }
    private String elapse(String sdate, String edate){
        String datediff = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(sdate);
            d2 = format.parse(edate);

            DateTime dt1 = new DateTime(d1);
            DateTime dt2 = new DateTime(d2);
            int hours = Hours.hoursBetween(dt1, dt2).getHours() % 24;
            int minutes = Minutes.minutesBetween(dt1, dt2).getMinutes() % 60;
            int seconds = Seconds.secondsBetween(dt1, dt2).getSeconds() % 60;

            if(hours != 0){
                datediff += hours + " hours, ";
            }
            datediff = String.format("%dmin, %ds", minutes,seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datediff;
    }

}
