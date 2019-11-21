package fitz;

import org.quartz.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ExpireAppJob implements Job {
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        java.util.Calendar currentDate = (java.util.Calendar) map.get("startDate");
        java.util.Calendar endDate = (java.util.Calendar) map.get("endDate");
        String strCurrentDate = dateFormat.format(currentDate.getTime());
        String strEndDate = dateFormat.format(endDate.getTime());

        if(strCurrentDate.equals(strEndDate)){
            try {
                Connection con = DriverManager.getConnection("jdbc:derby:"+AppDefault.EMBED_DBLOC.toString());
                con.createStatement().execute("insert into fitz (state) values (0)");
                jobExecutionContext.getScheduler().shutdown();
            } catch (SchedulerException | SQLException e) {
                e.printStackTrace();
            }
        }
        currentDate.add(Calendar.SECOND,10);
        map.put("startDate",currentDate);
    }
}
