package fitz;

import com.mchange.v2.log.MLogger;
import com.mchange.v2.log.slf4j.Slf4jMLog;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class Quartz {

    static void run() throws SchedulerException {

        Properties prop = new Properties();
        prop.setProperty("org.quartz.jobStore.class","org.quartz.impl.jdbcjobstore.JobStoreTX");
        prop.setProperty("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        prop.setProperty("org.quartz.jobStore.dataSource","fitz");
        prop.setProperty("org.quartz.scheduler.makeSchedulerThreadDaemon","true");
        //prop.setProperty("org.quartz.jobStore.misfireThreshold","1000");


        prop.setProperty("org.quartz.dataSource.fitz.driver","org.apache.derby.jdbc.EmbeddedDriver");
        prop.setProperty("org.quartz.dataSource.fitz.URL","jdbc:derby:"+AppDefault.EMBED_DBLOC);

        prop.setProperty("org.quartz.threadPool.class","org.quartz.simpl.SimpleThreadPool");
        prop.setProperty("org.quartz.threadPool.threadCount","1");


        Calendar startDate  = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH,1); //Will be changed to one month

        Scheduler scheduler = new StdSchedulerFactory(prop).getScheduler();
        MLogger logger = Slf4jMLog.getLogger();
        

        scheduler.start();

        boolean continue_ = true;

        List<String> jobGroups = scheduler.getJobGroupNames();

        for (String group: jobGroups
        ) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))){
                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                String job = jobGroup + "."+ jobName;
                if(job.equals("fitzGroup.expireAppJob")){
                    continue_ = false;
                }
            }
        }
        if(continue_) {
            JobDetail newJob = JobBuilder.newJob(ExpireAppJob.class)
                    .withIdentity("expireAppJob", "fitzGroup").requestRecovery(true)
                    .build();
            newJob.getJobDataMap().put("startDate", startDate);

            newJob.getJobDataMap().put("endDate", endDate);


            Trigger newTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("expireAppTrigger", "fitzGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever()
                            .withMisfireHandlingInstructionIgnoreMisfires()
                    )
                    .build();

            scheduler.scheduleJob(newJob, newTrigger);
        }
    }
    static Connection conEmbededDb() throws SQLException{
        Connection con;
        File loc = new File(AppDefault.EMBED_DBLOC.toString());
        String url = "";
        if(loc.exists() && loc.isDirectory()){
//            try{
//                Class.forName("org.apache.derby.jdbc.ClientDriver");
//            }
//            catch(Exception ex){
//                ex.printStackTrace();
//            }
            url = "jdbc:derby:"+AppDefault.EMBED_DBLOC.toString();
            con = DriverManager.getConnection(url);
            return con;
        }
        url = "jdbc:derby:"+AppDefault.EMBED_DBLOC.toString()+";create=true";
        con = DriverManager.getConnection(url);
        String[] importLi = {"create table qrtz_job_details (\n" +
                "sched_name varchar(120) not null,\n" +
                "job_name varchar(200) not null,\n" +
                "job_group varchar(200) not null,\n" +
                "description varchar(250) ,\n" +
                "job_class_name varchar(250) not null,\n" +
                "is_durable varchar(5) not null,\n" +
                "is_nonconcurrent varchar(5) not null,\n" +
                "is_update_data varchar(5) not null,\n" +
                "requests_recovery varchar(5) not null,\n" +
                "job_data blob,\n" +
                "primary key (sched_name,job_name,job_group)\n" +
                ")","create table qrtz_triggers(\n" +
                "sched_name varchar(120) not null,\n" +
                "trigger_name varchar(200) not null,\n" +
                "trigger_group varchar(200) not null,\n" +
                "job_name varchar(200) not null,\n" +
                "job_group varchar(200) not null,\n" +
                "description varchar(250),\n" +
                "next_fire_time bigint,\n" +
                "prev_fire_time bigint,\n" +
                "priority integer,\n" +
                "trigger_state varchar(16) not null,\n" +
                "trigger_type varchar(8) not null,\n" +
                "start_time bigint not null,\n" +
                "end_time bigint,\n" +
                "calendar_name varchar(200),\n" +
                "misfire_instr smallint,\n" +
                "job_data blob,\n" +
                "primary key (sched_name,trigger_name,trigger_group),\n" +
                "foreign key (sched_name,job_name,job_group) references qrtz_job_details(sched_name,job_name,job_group)\n" +
                ")","create table qrtz_simple_triggers(\n" +
                "sched_name varchar(120) not null,\n" +
                "trigger_name varchar(200) not null,\n" +
                "trigger_group varchar(200) not null,\n" +
                "repeat_count bigint not null,\n" +
                "repeat_interval bigint not null,\n" +
                "times_triggered bigint not null,\n" +
                "primary key (sched_name,trigger_name,trigger_group),\n" +
                "foreign key (sched_name,trigger_name,trigger_group) references qrtz_triggers(sched_name,trigger_name,trigger_group)\n" +
                ")","create table qrtz_cron_triggers(\n" +
                "sched_name varchar(120) not null,\n" +
                "trigger_name varchar(200) not null,\n" +
                "trigger_group varchar(200) not null,\n" +
                "cron_expression varchar(120) not null,\n" +
                "time_zone_id varchar(80),\n" +
                "primary key (sched_name,trigger_name,trigger_group),\n" +
                "foreign key (sched_name,trigger_name,trigger_group) references qrtz_triggers(sched_name,trigger_name,trigger_group)\n" +
                ")","create table qrtz_simprop_triggers\n" +
                "  (          \n" +
                "    sched_name varchar(120) not null,\n" +
                "    trigger_name varchar(200) not null,\n" +
                "    trigger_group varchar(200) not null,\n" +
                "    str_prop_1 varchar(512),\n" +
                "    str_prop_2 varchar(512),\n" +
                "    str_prop_3 varchar(512),\n" +
                "    int_prop_1 int,\n" +
                "    int_prop_2 int,\n" +
                "    long_prop_1 bigint,\n" +
                "    long_prop_2 bigint,\n" +
                "    dec_prop_1 numeric(13,4),\n" +
                "    dec_prop_2 numeric(13,4),\n" +
                "    bool_prop_1 varchar(5),\n" +
                "    bool_prop_2 varchar(5),\n" +
                "    primary key (sched_name,trigger_name,trigger_group),\n" +
                "    foreign key (sched_name,trigger_name,trigger_group) \n" +
                "    references qrtz_triggers(sched_name,trigger_name,trigger_group)\n" +
                ")","create table qrtz_blob_triggers(\n" +
                "sched_name varchar(120) not null,\n" +
                "trigger_name varchar(200) not null,\n" +
                "trigger_group varchar(200) not null,\n" +
                "blob_data blob,\n" +
                "primary key (sched_name,trigger_name,trigger_group),\n" +
                "foreign key (sched_name,trigger_name,trigger_group) references qrtz_triggers(sched_name,trigger_name,trigger_group)\n" +
                ")","create table qrtz_calendars(\n" +
                "sched_name varchar(120) not null,\n" +
                "calendar_name varchar(200) not null,\n" +
                "calendar blob not null,\n" +
                "primary key (sched_name,calendar_name)\n" +
                ")","create table qrtz_paused_trigger_grps\n" +
                "  (\n" +
                "    sched_name varchar(120) not null,\n" +
                "    trigger_group varchar(200) not null,\n" +
                "primary key (sched_name,trigger_group)\n" +
                ")","create table qrtz_fired_triggers(\n" +
                "sched_name varchar(120) not null,\n" +
                "entry_id varchar(95) not null,\n" +
                "trigger_name varchar(200) not null,\n" +
                "trigger_group varchar(200) not null,\n" +
                "instance_name varchar(200) not null,\n" +
                "fired_time bigint not null,\n" +
                "sched_time bigint not null,\n" +
                "priority integer not null,\n" +
                "state varchar(16) not null,\n" +
                "job_name varchar(200),\n" +
                "job_group varchar(200),\n" +
                "is_nonconcurrent varchar(5),\n" +
                "requests_recovery varchar(5),\n" +
                "primary key (sched_name,entry_id)\n" +
                ")","create table qrtz_scheduler_state\n" +
                "  (\n" +
                "    sched_name varchar(120) not null,\n" +
                "    instance_name varchar(200) not null,\n" +
                "    last_checkin_time bigint not null,\n" +
                "    checkin_interval bigint not null,\n" +
                "primary key (sched_name,instance_name)\n" +
                ")","create table qrtz_locks\n" +
                "  (\n" +
                "    sched_name varchar(120) not null,\n" +
                "    lock_name varchar(40) not null,\n" +
                "primary key (sched_name,lock_name)\n" +
                ")","create table fitz\n" +
                "  (          \n" +
                "    state int default 1,\n" +
                "    stage int default 1,\n" +
                "   \n" +
                "    primary key (state,stage)\n" +
                ")"};
        for (String import_: importLi
        ) {
            con.createStatement().execute(import_);
        }
        return con;
    }
}