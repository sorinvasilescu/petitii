package ro.petitii.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import ro.petitii.config.SchedulerConfig;
import ro.petitii.service.email.ImapService;

@Component
public class CronScheduler {
    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private ImapService imapService;

    @Autowired
    private SchedulerConfig schedulerConfig;

    public void setupScheduledTasks() {
        scheduler.schedule(new EmailChecker(imapService), new CronTrigger(schedulerConfig.emailCronPattern()));
    }
}
