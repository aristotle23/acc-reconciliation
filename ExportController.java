package fitz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExportController {

    @FXML
    private Button btn_export;

    @FXML
    private TableView<TableProperty> tbl_record;

    @FXML
    private TableColumn<TableProperty, String> col_date;

    @FXML
    private TableColumn<TableProperty, String> col_detail;

    @FXML
    private TableColumn<TableProperty, String> col_dr;

    @FXML
    private TableColumn<TableProperty, String> col_cr;

    @FXML
    private Label lbl_dr;

    @FXML
    private Label lbl_cr;

    private String type;
    private String color;
    private DatabaseHandler db;
    private Object[] currentTab;
    private XSSFWorkbook workbook;
    private File file;
    String from;
    String to;

    ExportController(String buttonId, Object[] currentTab,String from, String to){
        this.from = from;
        this.to = to;
        db = new DatabaseHandler();
        this.currentTab = currentTab;
        switch (buttonId){
            case "br_btn_pink":
                type = "csh_book";
                color = Color.PINK.toString();
                break;
            case "br_btn_blue":
                type = "bnk_statement";
                color = Color.BLUE.toString();
                break;
            case "br_btn_green":
                type = "bnk_statement";
                color = Color.GREEN.toString();
                break;
            case "br_btn_white_csh_book":
                type = "csh_book";
                color = Color.WHITE.toString();
                break;
            case "br_btn_white_bnk_statement":
                type = "bnk_statement";
                color = Color.WHITE.toString();
                break;
            case "br_btn_red_csh_book":
                type = "csh_book";
                color = Color.RED.toString();
                break;
            case "br_btn_red_bnk_statement":
                type = "bnk_statement";
                color = Color.RED.toString();
                break;
        }

    }

    @FXML
    void initialize() throws SQLException {

        String sql = String.format("select * from %s where color = ? and account_id = ? and ( date between ? and ? )",type);
        ResultSet resultLi = db.getAll(sql, new Object[]{color,currentTab[0],from,to});

        // Blank workbook
        workbook = new XSSFWorkbook();

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Export Sheet");

        String[] header = {"Date","Detail","Dr","Cr"};
        int rownum = 0;
        Row row = sheet.createRow(rownum++);
        for (int i = 0 ; i <header.length; i++) {
            org.apache.poi.ss.usermodel.Cell dateCell = row.createCell(i);
            dateCell.setCellValue(header[i]);
            dateCell.setCellStyle(headerCellStyle);

        }
        double ttlDr = 0.00;
        double ttlCr = 0.00;


        while (resultLi.next()){
            String date = resultLi.getDate("date").toString();
            String detail = resultLi.getString("detail");
            String debit = String.format("%.2f", resultLi.getFloat("debit"));
            String credit = String.format("%.2f", resultLi.getFloat("credit"));
            String color = resultLi.getString("color");
            String ref = resultLi.getString("ref");
            int id = resultLi.getInt("id");
            ttlDr += resultLi.getDouble("debit");
            ttlCr += resultLi.getDouble("credit");

            row = sheet.createRow(rownum++);
            org.apache.poi.ss.usermodel.Cell dateCell = row.createCell(0);
            dateCell.setCellValue(date);
            org.apache.poi.ss.usermodel.Cell detailCell = row.createCell(1);
            detailCell.setCellValue(detail);
            org.apache.poi.ss.usermodel.Cell drCell = row.createCell(2);
            drCell.setCellValue(resultLi.getDouble("debit"));
            org.apache.poi.ss.usermodel.Cell crCell = row.createCell(3);
            crCell.setCellValue(resultLi.getDouble("credit"));


            tbl_record.getItems().add(new TableProperty(date,detail,debit,credit,color,ref));
        }
        row = sheet.createRow(rownum);
        org.apache.poi.ss.usermodel.Cell dateCell = row.createCell(0);
        dateCell.setCellValue("Total");
        dateCell.setCellStyle(headerCellStyle);
        org.apache.poi.ss.usermodel.Cell drCell = row.createCell(2);
        drCell.setCellValue(ttlDr);
        drCell.setCellStyle(headerCellStyle);
        org.apache.poi.ss.usermodel.Cell crCell = row.createCell(3);
        crCell.setCellValue(ttlCr);
        crCell.setCellStyle(headerCellStyle);

        col_date.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("date"));
        col_detail.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("detail"));
        col_dr.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("debit"));
        col_cr.setCellValueFactory(new PropertyValueFactory<TableProperty,String>("credit"));

        tbl_record.setRowFactory(tv -> new TableRow<TableProperty>() {
            @Override
            protected void updateItem(TableProperty item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {

                    if (item.getColor().equals(Color.RED.toString())) {
                        setStyle(String.format("-fx-background-color: %s;", Color.RED.toString()));
                    }else  if(item.getColor().equals(Color.PINK.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.PINK.toString()));
                    }else if(item.getColor().equals(Color.BLUE.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.BLUE.toString()));
                    }else if(item.getColor().equals(Color.GREEN.toString())){
                        setStyle(String.format("-fx-background-color: %s;",Color.GREEN.toString()));
                    }
                }

            }
        });

        lbl_cr.setText(String.format("%.2f",ttlCr));
        lbl_dr.setText(String.format("%.2f",ttlDr));
    }

    /*ResultSet getResultLi() throws SQLException {
        if(!resultLi.next()) return  null;
        return resultLi;
    }*/

    public File getFile() {
        return file;
    }


    @FXML
    void exportRecord(ActionEvent event) {
        Button export = (Button) event.getSource();
        Stage stage = (Stage) export.getScene().getWindow();

        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("Export Record");
        fileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx"));

        file = fileDialog.showSaveDialog(stage);

        try {
            // this Writes the workbook gfgcontribute
            FileOutputStream out = new FileOutputStream(new File(file.toString()));
            workbook.write(out);
            out.close();
            System.out.println(String.format("%s exported successfully ",file.toString()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        stage.close();
    }

}
