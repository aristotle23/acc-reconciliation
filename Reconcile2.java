package fitz;

import javafx.concurrent.Task;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Reconcile2 extends Task {
    private  DatabaseHandler db = new DatabaseHandler();
    private int accId;

    Reconcile2(int accId){
        this.accId = accId;
    }

    public Object call() throws SQLException {
        matching(1);
        multiMatching();
        unMatched();

        return true;
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
                //System.out.println(ranking.get(keys));
            }

        }
        if(step < 2) {

            matching(step + 1);
        }

    }
    private void  multiMatching() throws SQLException {
        Object[] param= {accId};
        ResultSet cshLi = db.getAll("SELECT * FROM csh_book WHERE account_id = ? and bnk_statement_id = 0",param);
        while (cshLi.next()){
            TreeMap<Double, Object[]> ranking = new TreeMap<>(Comparator.reverseOrder()); //use score as key and bank statement ID as value;

            int cshId = cshLi.getInt("id");
            Date cshDate = cshLi.getDate("date");
            String cshDetail = cshLi.getString("detail");
            float cshDebit = cshLi.getFloat("debit");
            float cshCredit = cshLi.getFloat("credit");
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

                /*System.out.println("highest ranking "+ keys);
                System.out.println("Bank Statement ID ----- "+ bnkId);*/


                Object[] bnkParam = {color,bnkId};
                Object[] cshParam = {bnkRef,bnkId,color,cshId};
                db.execute("update bnk_statement set color = ? where id = ?",bnkParam);
                db.execute("update csh_book set ref = ?, bnk_statement_id = ?, color = ? where id = ?",cshParam);
                //db.execute("update csh_book set color = ? where bnk_statement_id = ?",bnkParam);

            }

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
        }

    }

    private String generateRef(String tag) throws SQLException {
        String text = "abcdefghijklmnopqrstvuwxyz0123456789:.-+|#@&*!";
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
}
