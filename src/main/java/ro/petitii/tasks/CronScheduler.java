package ro.petitii.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import ro.petitii.config.DeadlineConfig;
import ro.petitii.config.SchedulerConfig;
import ro.petitii.config.ServerConfig;
import ro.petitii.config.SmtpConfig;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;
import ro.petitii.service.email.ImapService;
import ro.petitii.service.email.SmtpService;
import ro.petitii.service.template.EmailTemplateProcessorService;

@Component
public class CronScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CronScheduler.class);

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private ImapService imapService;

    @Autowired
    private SmtpService smtpService;

    @Autowired
    private EmailTemplateProcessorService processorService;

    @Autowired
    private PetitionService petitionService;

    @Autowired
    private UserService userService;

    @Autowired
    private SchedulerConfig schedulerConfig;

    @Autowired
    private DeadlineConfig deadlineConfig;

    @Autowired
    private SmtpConfig smtpConfig;

    @Autowired
    private ServerConfig config;

    public void setupScheduledTasks() {
        if (schedulerConfig.emailCronPattern() != null) {
            logger.info("Successfully scheduled the email checker: " + schedulerConfig.emailCronPattern());
            scheduler.schedule(new EmailChecker(imapService), new CronTrigger(schedulerConfig.emailCronPattern()));
        } else {
            logger.info("The email checker is not running ...");
        }

        if (schedulerConfig.deadlineCronPattern() != null) {
            if (config.serverUrl() == null) {
                logger.error("Invalid base server url, the deadline checker is not running ...");
            } else {
                logger.info("Successfully scheduled the deadline checker: " + schedulerConfig.deadlineCronPattern());
                scheduler.schedule(new DeadlineChecker(userService, petitionService, smtpService, processorService,
                                                       smtpConfig, deadlineConfig, config.serverUrl()),
                                   new CronTrigger(schedulerConfig.deadlineCronPattern()));
            }
        } else {
            logger.info("The deadline checker is not running ...");
        }
    }
}
