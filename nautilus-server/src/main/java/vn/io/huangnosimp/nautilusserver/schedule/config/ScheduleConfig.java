package vn.io.huangnosimp.nautilusserver.schedule.config;

import java.util.TimeZone;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.io.huangnosimp.nautilusserver.schedule.job.DeleteTrashResourceJob;

@Configuration
public class ScheduleConfig {
    @Bean
    public JobDetail deleteTrashResourceJobDetail() {
        return JobBuilder.newJob(DeleteTrashResourceJob.class)
                .withIdentity("deleteTrashResourceJob", "storage")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger deleteTrashResourceJobTrigger(JobDetail deleteTrashResourceJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(deleteTrashResourceJobDetail)
                .withIdentity("deleteTrashResourceTrigger", "storage")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 3 * * ?")
                                .inTimeZone(
                                        TimeZone.getTimeZone(
                                                "Asia/Ho_Chi_Minh"))) // Run every day at 3 AM
                .build();
    }
}
