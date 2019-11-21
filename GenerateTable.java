package fitz;

import java.sql.ResultSet;

public class GenerateTable  {
    private int accId;
    private NewAppTabController tab;
    private DatabaseHandler db;
    private String type;
    GenerateTable(int accId, NewAppTabController tab, String type){
        this.accId = accId;
        this.tab = tab;
        this.type = type;
        this.db = new DatabaseHandler();
    }

    void call() throws Exception {


        db.execute(String.format("UPDATE %s SET display = ? WHERE account_id = ? ", this.type),new Object[]{0, accId}); //update the display indicating co record is being displayed


        Object param[] = {accId};
        String sql = String.format("SELECT * FROM %s where ref is not  NULL and account_id = ? and display = 0 ", this.type);
        ResultSet result = db.getAll(sql,param);
        while (result.next()) {
            String date = result.getDate("date").toString();
            String detail = result.getString("detail");
            String debit = String.format("%.2f", result.getFloat("debit"));
            String credit = String.format("%.2f", result.getFloat("credit"));
            String color = result.getString("color");
            String ref = result.getString("ref");
            int id = result.getInt("id");

            switch (type) {
                case "bnk_statement":
                    //tab.tbl_bk.getItems().add(new TableProperty(date, detail, debit, credit,color,ref));
                    tab.bkCollection.add(new TableProperty(date, detail, debit, credit,color,ref));
                    break;
                case "csh_book":
                    tab.cbCollection.add(new TableProperty(date, detail, debit, credit,color,ref));
                    //tab.tbl_cb.getItems().add(new TableProperty(date, detail, debit, credit,color,ref));
            }
            sql = String.format("UPDATE %s SET display = ? WHERE id = ? ", this.type);
            db.execute(sql,new Object[]{1, id});
            //tbl_cb.getItems().add(new CbProperty("00-00-2019","The detail of...",1053.00,100.00));

        }
        //Thread.sleep(10);
        /*sql = String.format("SELECT * FROM %s where ref is NULL and account_id = ?", this.type);
        result = db.getAll(sql,param);
        if(!result.next()) break;*/


    }
}
