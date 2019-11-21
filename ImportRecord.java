package fitz;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



public class ImportRecord extends Task<Boolean> {
    private String importType;
    private String[] filepath;
    private int accountId;
    private XSSFSheet sheet;
    private int headerIndex;
    private DatabaseHandler db;
    public Map<String, ArrayList> validHeader;
    private Label lbl_progressBar;
    private Label op_status;
    private Double lastBalance = 0.00;
    public ImportRecord(String filepath, String type, int accountId, Label lbl_progressBar, Label op_status){
        db = new DatabaseHandler();
        this.filepath = filepath.split(";");
        this.importType = type;
        this.accountId = accountId;
        this.lbl_progressBar = lbl_progressBar;
        this.op_status = op_status;

    }
    @Override
    protected Boolean call() throws Exception {
        Platform.runLater(() -> lbl_progressBar.setText("Getting header index"));
        validHeaderType(); //get the valid header column labelling convention and index;
        ArrayList<String[]> header = getHeader();

        if(header.size() != 5){
            Platform.runLater(() -> {
                Stage stage = (Stage) lbl_progressBar.getScene().getWindow();
                stage.close();
                op_status.setText("Import Failed : Invalid document structure");
            });

            return  false;
        }
        int firstRow = headerIndex + 1;
        int lastRow = sheet.getLastRowNum();
        int maximumRow = lastRow - firstRow;
        int importLog_id = 0;
        int recordId = 0;
        for(int i = firstRow; i <= lastRow; i++){
            if(isCancelled()){
                updateMessage("Cancelled");
                return true;
            }
            Row row = sheet.getRow(i);
            if(row == null){
                continue;
            }
            String date = null;
            String detail = null;
            String raw_detail;
            Double credit = null;
            Double debit = null;
            Double balance = null;
            String type ;
            for(int c = 0 ; c < header.size(); c++){

                Cell cell = row.getCell(Integer.parseInt(header.get(c)[0]));
                type = header.get(c)[1];
                if(cell == null){
                    continue;
                }
                switch (type){
                    case "date":
                        date = cell.toString();
                        break;
                    case "detail":
                        detail = cell.toString();
                        break;
                    case "debit":
                        if(cell.toString().equals("")){
                            debit = 0.0;
                        }else{
                            debit = Double.parseDouble(cell.toString());
                        }
                        break;
                    case "credit":
                        if(cell.toString().equals("")){
                            credit = 0.0;
                        }else {
                            credit = Double.parseDouble(cell.toString());
                        }
                        break;
                    case "balance":
                        if(cell.toString().equals("")){
                            balance = 0.0;
                        }else {
                            balance = Double.parseDouble(cell.toString());
                        }

                }
            }
            if (date == null || detail == null ) {
                Thread.sleep(10); //Where there no record in row pause the this thread for 10 milisecond to mimic little data in row
                updateProgress(i,maximumRow);
                continue;
            }

            Date utildate ;
            debit = (debit == null) ? 0.00 : debit;
            credit = (credit == null) ? 0.00 : credit;
            balance = (balance == null) ? 0.00 : balance;

            try {
                SimpleDateFormat importFormat = new SimpleDateFormat("dd-MMM-yyyy");
                SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MMM-dd");
                utildate = sqlFormat.parse(sqlFormat.format(importFormat.parse(date)));
            } catch (ParseException e) {
                Platform.runLater(() -> {
                    Stage stage = (Stage) lbl_progressBar.getScene().getWindow();
                    stage.close();
                    op_status.setText("Import Failed : Invalid document structure");
                });
                return false;
            }
            WordSanitizer sanitizer = new WordSanitizer();
            raw_detail = detail;
            detail = sanitizer.sanitize(detail); //remove unwanted characters like numbers and symbols
            int bfwd = isBalFwd(detail, debit, credit);
            if(i == firstRow) {
                /*
                check if account already have imported record
                 */
                ResultSet checkAccount = db.getAll(String.format("SELECT balance FROM %s where account_id = ? order by id desc limit 1;", importType), new Object[]{accountId});
                if (checkAccount.next()) {
                    if(bfwd == 1) continue;
                    lastBalance = checkAccount.getDouble("balance");
                } else {
                    /*
                        check if current row is balance brought forward
                        if it is use is balance as the last balance;
                     */
                    if (bfwd == 0) {
                        Platform.runLater(() -> {
                            Stage stage = (Stage) lbl_progressBar.getScene().getWindow();
                            stage.close();
                            op_status.setText("Import Failed : Invalid document structure");
                        });
                        return false;
                    }
                    lastBalance = balance;
                }
            }
            lastBalance = getBalance(debit,credit);
            String ref = getRef(raw_detail);

            Object[] param = {utildate,detail,debit,credit,accountId,raw_detail,bfwd,lastBalance,ref};
            //wordCounter(detail);

            String sql = String.format("INSERT INTO `%s` (`date`, `detail`, `debit`, `credit`, `account_id`, `raw_detail`,`bfwd`,`balance`, `ref`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",importType); //format string to include database
            recordId = db.executeGetId(sql,param);
            if(i == firstRow){
                importLog_id = db.executeGetId("INSERT INTO import_log (`start`, `account_id`, `type`, `excel_row` ) VALUES (?, ?, ?, ?);",new Object[]{recordId,accountId,importType,i});
            }else{
                db.execute("update import_log set excel_row = ? where id = ?",new Object[]{i,importLog_id});
            }
            Platform.runLater(() -> lbl_progressBar.setText("Importing records. Please wait...."));
            updateProgress(i,maximumRow);


        }
        if(importLog_id != 0 && recordId != 0 ) {
            db.execute("update import_log set end = ?, end_time = CURRENT_TIMESTAMP() where id = ?", new Object[]{recordId, importLog_id});
        }
        return true;
    }

    /**
     * get the ref of the current record in the raw detail
     * @param rawDetail parameter for the raw detail
     * @return return null if there is no ref in the raw detail or the ref of the record
     */
    private String getRef(String rawDetail){
        String ref = null;

        String[] detailLi = rawDetail.toLowerCase().split(" ");
        for ( String detail : detailLi
             ) {
            if(detail.startsWith("#")){
                ref = detail;
            }
        }

        return  ref;
    }
    /**
     * check if current record is balance brought foward
     * @param detail the detail to check if tis balance brought forward
     * @param debit value must be 0 if its balance brought forward
     * @param credit value must be 0 if its balance brought forward
     * @return return 1 or 0.. 1 represent balance brought forward and 0 represent it's not balance brought forward
     */
    private int isBalFwd(String detail,Double debit, Double credit){
        boolean brought = detail.toLowerCase().contains("brought");
        boolean forward = detail.toLowerCase().contains("forward");

        if(brought && forward && debit == 0.0 && credit == 0.0){
            return 1;
        }
        return 0;
    }

    /**
     * get the balance of the current record
     * @param debit the debit of the record
     * @param credit the creidt of the record
     * @return the balance of the current record
     */
    private  double getBalance(double debit, double credit){
        Double balance = 0.00;
        switch (importType){
            case "csh_book":
                balance = (debit + lastBalance) - credit;
                break;
            case "bnk_statement":
                balance = (credit + lastBalance) - debit;
        }
        return  balance;
    }


    /**
     *
     * @return table header cell label convention and their index in the rows
     * This function mimic get the valid label from config file or database
     */
    private void validHeaderType(){

        String[] dateLi = "date trandate".toLowerCase().split(" ");
        String[] detailLi= "desc description detail memo".toLowerCase().split(" ");
        String[] debitLi = "Debit Dr ".toLowerCase().split(" ");
        String[] creditLi = "Credit Cr".toLowerCase().split(" ");
        String[] balanceLi = "Balance bal".toLowerCase().split(" ");

        ArrayList<String> date = new ArrayList<>();
        ArrayList<String> detail = new ArrayList<>();
        ArrayList<String> credit = new ArrayList<>();
        ArrayList<String> debit = new ArrayList<>();
        ArrayList<String> balance = new ArrayList<>();

        Collections.addAll(date, dateLi);
        Collections.addAll(detail,detailLi);
        Collections.addAll(debit,debitLi);
        Collections.addAll(credit,creditLi);
        Collections.addAll(balance,balanceLi);


        validHeader = new HashMap();
        validHeader.put("date",date);
        validHeader.put("detail",detail);
        validHeader.put("credit",credit);
        validHeader.put("debit",debit);
        validHeader.put("balance",balance);

    }

    /**
     * get the the header of the import
     * @return ArrayList of the header
     */
    private ArrayList getHeader() {
        ArrayList<String[]> headerLi = new ArrayList<>();
        try{
            for(String file : this.filepath){
                FileInputStream fileStream;
                fileStream = new FileInputStream( new File(file));
                XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
                this.sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                while (rowIterator.hasNext()){

                    Row row = rowIterator.next();

                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String txt_cell = cell.toString().toLowerCase();


                        ArrayList date = validHeader.get("date");
                        ArrayList detail = validHeader.get("detail");
                        ArrayList debit = validHeader.get("debit");
                        ArrayList credit = validHeader.get("credit");
                        ArrayList balance = validHeader.get("balance");

                        Integer cellIndex = cell.getColumnIndex();

                        if(date.contains(txt_cell)){
                            String[] a = {cellIndex.toString(), "date"};
                            headerLi.add(a);
                        }
                        if(detail.contains(txt_cell)){
                            String[] a = {cellIndex.toString(), "detail"};
                            headerLi.add(a);

                        }
                        if(debit.contains(txt_cell)){
                            String[] a = {cellIndex.toString(), "debit"};
                            headerLi.add(a);
                        }
                        if(credit.contains(txt_cell)){
                            String[] a = {cellIndex.toString(), "credit"};
                            headerLi.add(a);
                        }
                        if(balance.contains(txt_cell)){
                            String[] a = {cellIndex.toString(), "balance"};
                            headerLi.add(a);
                        }
                    }
                    if(headerLi.size() >= 5){
                        headerIndex = row.getRowNum();
                        break;
                    }
                    headerLi.clear();
                }
                fileStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return headerLi;
    }
    /*private void wordCounter(String sentence) throws SQLException {
        String[] wordLi = sentence.split(" ");
        for ( String word: wordLi ) {
            Object[] param = {word,accountId};
            ResultSet check = db.getAll("select * from word_list where word = ? and account_id = ? limit 1", param);
            if(check.next()){
                Object[] checkId = {check.getInt("id")};
                db.execute("update word_list set count = count + 1 where id = ?",checkId);
            }else {
                db.execute("INSERT INTO `word_list` (`word`, `account_id`) VALUES (?, ?);",param);

            }

        }

    }*/

}
