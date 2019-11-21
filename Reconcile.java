package fitz;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Reconcile extends Task {
    private  DatabaseHandler db = new DatabaseHandler();
    private int accId;
    private NewAppTabController tab;
    private int refresh;
    Reconcile(int accId, NewAppTabController tab, int refresh){
        
        this.accId = accId;
        this.tab = tab;
        this.refresh = refresh;
    }

    public Object call() throws SQLException {
        if(refresh == 0){
            generateTb();
        }
        refMatch();
        matching(1);
        unMatched();
        Platform.runLater(() -> {
            tab.tbl_bk.getSortOrder().add(tab.tbl_bk_date);
            tab.tbl_cb.getSortOrder().add(tab.tbl_cb_date);
        });


        return true;
    }
    private  void generateTb() throws SQLException {
        db.execute("UPDATE csh_book SET display = ? WHERE account_id = ? ",new Object[]{0, accId}); //update the display indicating co record is being displayed
        db.execute("UPDATE bnk_statement SET display = ? WHERE account_id = ? ",new Object[]{0, accId}); //update the display indicating co record is being displayed
        
        ResultSet result = db.getAll("SELECT b.id as bnk_id,c.id as csh_id FROM bnk_statement b inner join csh_book c " +
                "WHERE b.csh_book_id = c.id AND c.display = 0  AND c.account_id = ?", new Object[]{accId});
        while (result.next()) {
            int cshId = result.getInt("csh_id");
            int bnkId = result.getInt("bnk_id");
            updateTable(cshId,"csh_book");
            updateTable(bnkId, "bnk_statement");
        }

    }
    private void refMatch() throws SQLException {
        Object[] param= {accId};
        ResultSet cshLi = db.getAll("SELECT * FROM csh_book WHERE bnk_statement_id = 0 AND account_id = ? AND ref IS NOT NULL",param);
        while (cshLi.next()){
            String color = Color.WHITE.toString();
            String cshRef = cshLi.getString("ref");
            int cshId = cshLi.getInt("id");
            double cshDebit = cshLi.getDouble("debit");
            double cshCredit = cshLi.getDouble("credit");
            ResultSet bnkLi = db.getAll("SELECT * FROM bnk_statement WHERE ref = ? AND account_id = ? AND csh_book_id = 0", new Object[]{cshRef,accId});
            if(!bnkLi.next() ) continue;
            int bnkId = bnkLi.getInt("id");
            double bnkCredit = bnkLi.getDouble("credit");
            double bnkDebit = bnkLi.getDouble("debit");

            if(cshDebit != bnkCredit || cshCredit != bnkDebit){
                color = Color.RED.toString();
            }else{
                color = Color.WHITE.toString();
            }
            /*
            get the total csh_book with same ref code
             */
            ResultSet countCshRef = db.getAll("SELECT count(ref) AS refcount FROM csh_book WHERE ref = ? AND account_id = ? ", new Object[]{cshRef,accId});
            countCshRef.next();
            if(countCshRef.getInt("refcount") > 1){
                color = Color.GREY.toString();
            }

            Object[] bnkParam = {cshId,color,bnkId};
            Object[] cshParam = {bnkId,color,cshId};
            db.execute("update bnk_statement set csh_book_id = ?, color = ? where id = ?",bnkParam);
            db.execute("update csh_book set bnk_statement_id = ?, color = ? where id = ?",cshParam);
            updateTable(cshId,"csh_book");
            updateTable(bnkId,"bnk_statement");

        }
    }
    private void updateTable(int idx, String type) throws SQLException {


        ResultSet result = db.getAll(String.format("SELECT * FROM %s where id = ?", type),new Object[]{idx});
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
            db.execute(String.format("UPDATE %s SET display = ? WHERE id = ? ",type),new Object[]{1, id});


        }
    }
    private void matching (int step) throws SQLException {
        Object[] param= {accId};
        ResultSet cshLi = db.getAll("SELECT * FROM csh_book WHERE account_id = ? and bnk_statement_id = 0",param);
        while (cshLi.next()){
            TreeMap<Double, Object[]> ranking = new TreeMap<>(Comparator.reverseOrder()); //use score as key and bank statement ID as value;

            int cshId = cshLi.getInt("id");
            Date cshDate = cshLi.getDate("date");
            String cshDetail = cshLi.getString("detail");
            float cshDebit = cshLi.getFloat("debit");
            float cshCredit = cshLi.getFloat("credit");
            String bnkSql = "SELECT * FROM bnk_statement where account_id = ? and csh_book_id = 0";
            if(cshCredit == 0 && cshDebit > 0){
                bnkSql = "SELECT * FROM bnk_statement WHERE debit = 0 and credit > 0 and account_id = ? and csh_book_id = 0";
            }else if(cshCredit > 0 && cshDebit == 0){
                bnkSql = "SELECT * FROM bnk_statement WHERE debit > 0 and credit = 0 and account_id = ? and csh_book_id = 0";
            }

            //System.out.println(cshDate +" "+cshDetail + " " + cshDebit + " " + cshCredit );
            //System.out.println(bnkSql);
            ResultSet bnkLi = db.getAll(bnkSql,param);
            while (bnkLi.next()){
                int bnkId = bnkLi.getInt("id");
                Date bnkDate = bnkLi.getDate("date");
                String bnkDetail = bnkLi.getString("detail");
                float bnkDebit = bnkLi.getFloat("debit");
                float bnkCredit = bnkLi.getFloat("credit");
                double score = similarity(cshDetail,bnkDetail);

                if(score >= 0.6){

                    if(cshDebit == bnkCredit && cshCredit == bnkDebit){ //check if the figures matches
                        score += 0.3;
                    }
                    score += dateScore(cshDate.toString(),bnkDate.toString());
                    switch (step){
                        case 1:
                            if (score >= 1.0){
                                ranking.put(score,new Object[]{bnkId,bnkDebit,bnkCredit});
                            }
                            break;
                        case 2:
                            if (score >= 0.8 && score < 1.0){
                                ranking.put(score,new Object[]{bnkId,bnkDebit,bnkCredit});
                            }
                            break;
                    }
                    //System.out.println("---- "+ score + " "+bnkDate +" "+bnkDetail + " " + bnkDebit + " " + bnkCredit );

                }
            }

            //selection starts here
            if (ranking.size() > 0) {
                Double keys = ranking.firstKey();
                String ref = generateRef("");
                String color = Color.WHITE.toString();
                /*System.out.println("highest ranking "+ keys);
                System.out.println("Bank Statement ID ----- "+ ranking.get(keys));*/

                Object[] matchedBnk = ranking.get(keys);
                float matchedBnkDebit = (float) matchedBnk[1];
                float matchedBnkCredit = (float) matchedBnk[2];
                if(cshDebit != matchedBnkCredit || cshCredit != matchedBnkDebit){
                    color = Color.RED.toString();
                }


                Object[] bnkParam = {ref,cshId,color,matchedBnk[0]};
                Object[] cshParam = {ref,matchedBnk[0],color,cshId};
                db.execute("update bnk_statement set ref = ?, csh_book_id = ?, color = ? where id = ?",bnkParam);
                db.execute("update csh_book set ref = ?, bnk_statement_id = ?, color = ? where id = ?",cshParam);
                updateTable(cshId,"csh_book");
                updateTable((int) matchedBnk[0],"bnk_statement");
                //System.out.println(ranking.get(keys));
            }

        }
        if(step < 2) {

            matching(step + 1);
        }

    }


    private  void  unMatched() throws SQLException {
        ResultSet cshLi = db.getAll("select * from csh_book where account_id = ? and bnk_statement_id = 0",new Object[]{accId});
        while ( cshLi.next()){
            String ref = generateRef("");
            String color = Color.PINK.toString();
            int cshId = cshLi.getInt("id");
            Object[] cshParam = {color,ref,cshId};
            db.execute("update csh_book set color = ?, ref = ? where id = ?",cshParam);
            updateTable(cshId,"csh_book");
        }
        ResultSet bnkLi = db.getAll("select * from bnk_statement where account_id = ? and csh_book_id = 0",new Object[]{accId});
        while ( bnkLi.next()){
            String color;
            String ref = generateRef("");
            float debit = bnkLi.getFloat("debit");
            float credit = bnkLi.getFloat("credit");
            int bnkId = bnkLi.getInt("id");
            if (credit > debit){
                color = Color.GREEN.toString();
            }else{
                color = Color.BLUE.toString();
            }
            Object[] bnkParam = {color,ref,bnkId};
            db.execute("update bnk_statement set color = ?, ref = ? where id = ?",bnkParam);
            updateTable(bnkId,"bnk_statement");
        }

    }

    private String generateRef(String tag) throws SQLException {
        String text = "abcdefghijklmnopqrstvuwxyz0123456789:|@-&_*!+~";
        List<String> chars = Arrays.asList(text.split(""));
        Collections.shuffle(chars);
        String ref = tag + chars.get(0) + chars.get(10) + chars.get(30);
        Object[] param = {ref,accId};
        ResultSet chckRef = db.getAll("select ref from bnk_statement where ref = ? and account_id = ?",param);
        if(chckRef.next()){
            generateRef(chars.get(15));
        }
        return  ref;
    }

    /**
     *
     * @param cshDate the cash book date
     * @param bnkDate the bank statement date
     * @return the date score between the two date
     */
    private double dateScore(String cshDate, String bnkDate){
       double dateScore = 0.15;
       double diff = ChronoUnit.DAYS.between(LocalDate.parse(cshDate), LocalDate.now());
       double aDateScore = dateScore/diff;
       double dateDiff = ChronoUnit.DAYS.between(LocalDate.parse(cshDate),LocalDate.parse(bnkDate));
       dateScore -= aDateScore * dateDiff;
       return dateScore;

    }
    private   double similarity(String leftStr, String rightStr){

        JaroWinklerDistance distance = new JaroWinklerDistance();
        return  distance.apply(leftStr,rightStr);
    }
    /*private void  multiMatching() throws SQLException {
        Object[] param= {accId};
        ResultSet cshLi = db.getAll("SELECT * FROM csh_book WHERE account_id = ? and bnk_statement_id = 0",param);
        while (cshLi.next()){
            TreeMap<Double, Object[]> ranking = new TreeMap<>(Comparator.reverseOrder()); //use score as key and bank statement ID as value;

            int cshId = cshLi.getInt("id");
            Date cshDate = cshLi.getDate("date");
            String cshDetail = cshLi.getString("detail");
            *//*float cshDebit = cshLi.getFloat("debit");
            float cshCredit = cshLi.getFloat("credit");*//*
            String bnkSql = "SELECT * FROM bnk_statement where account_id = ? and csh_book_id != 0 and ( color = ? or color = ? )";
            //System.out.println(cshDate +" "+cshDetail + " " + cshDebit + " " + cshCredit );
            //System.out.println(bnkSql);
            ResultSet bnkLi = db.getAll(bnkSql,new Object[]{accId,Color.RED.toString(),Color.GREY.toString()});
            while (bnkLi.next()){
                int bnkId = bnkLi.getInt("id");
                Date bnkDate = bnkLi.getDate("date");
                String bnkDetail = bnkLi.getString("detail");

                String bnkRef = bnkLi.getString("ref");
                double score = similarity(cshDetail,bnkDetail);

                if(score >= 0.6){
                    score += dateScore(cshDate.toString(),bnkDate.toString());
                    if (score >= 0.8 && score < 1.0){
                        ranking.put(score,new Object[]{bnkId,bnkRef});
                    }
                    //System.out.println("---- "+ score + " "+bnkDate +" "+bnkDetail + " " + bnkDebit + " " + bnkCredit );

                }
            }

            //selection starts here
            if (ranking.size() > 0) {
                Double keys = ranking.firstKey();
                Object[] mtchBnk = ranking.get(keys);
                String bnkRef = (String) mtchBnk[1];
                int bnkId = (int) mtchBnk[0];
                String color = Color.GREY.toString();

                *//*System.out.println("highest ranking "+ keys);
                System.out.println("Bank Statement ID ----- "+ bnkId);*//*


                Object[] bnkParam = {color,bnkId};
                Object[] cshParam = {bnkRef,bnkId,color,cshId};
                db.execute("update bnk_statement set color = ? where id = ?",bnkParam);
                db.execute("update csh_book set ref = ?, bnk_statement_id = ?, color = ? where id = ?",cshParam);
                //db.execute("update csh_book set color = ? where bnk_statement_id = ?",bnkParam);

            }
        }

    }*/
}
