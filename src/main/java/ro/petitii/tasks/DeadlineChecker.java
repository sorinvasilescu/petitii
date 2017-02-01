package ro.petitii.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.petitii.config.DeadlineConfig;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;
import ro.petitii.service.email.SmtpService;
import ro.petitii.service.template.EmailTemplateProcessorService;

import javax.mail.MessagingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static ro.petitii.util.DateUtil.alertStatus;

public class DeadlineChecker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadlineChecker.class);
    private static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    private UserService userService;
    private PetitionService petitionService;
    private SmtpService smtpService;
    private EmailTemplateProcessorService emailTemplateProcessorService;

    private SmtpConfig smtpConfig;
    private DeadlineConfig deadlineConfig;

    private String serverUrl;

    DeadlineChecker(UserService userService, PetitionService petitionService, SmtpService smtpService,
                    EmailTemplateProcessorService processorService, SmtpConfig smtpConfig,
                    DeadlineConfig deadlineConfig, String serverUrl) {
        this.userService = userService;
        this.petitionService = petitionService;
        this.smtpService = smtpService;
        this.emailTemplateProcessorService = processorService;
        this.deadlineConfig = deadlineConfig;
        this.smtpConfig = smtpConfig;
        this.serverUrl = serverUrl;
    }

    @Override
    public void run() {
        LOGGER.info("Running the deadline checker ...");
        for (User user : userService.getAllUsers()) {
            if (user.getRole() != User.UserRole.SUSPENDED) {
                List<Petition> petitions = petitionService.findAllByResponsible(user);
                if (petitions != null && !petitions.isEmpty()) {
                    Map<String, List<Petition>> notifications = petitionNotifications(petitions);
                    if (!notifications.isEmpty()) {
                        notifyUser(user, notifications);
                    }
                }
            }
        }
    }

    private Map<String, List<Petition>> petitionNotifications(List<Petition> petitions) {
        Map<String, List<Petition>> result = new HashMap<>();
        Date today = new Date();
        for (Petition petition : petitions) {
            String alert = alertStatus(petition.getCurrentStatus(), today, petition.getDeadline(), deadlineConfig);
            if (!alert.isEmpty() && !alert.equalsIgnoreCase("green")) {
                List<Petition> alertList = result.computeIfAbsent(alert, k -> new LinkedList<>());
                alertList.add(petition);
            }
        }
        return result;
    }

    private void notifyUser(User user, Map<String, List<Petition>> notifications) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("today", df.format(new Date()));
        vars.put("notifications", notifications);
        vars.put("user", user);
        vars.put("serverUrl", serverUrl);
        String emailBody = emailTemplateProcessorService.processStaticTemplate("deadline_notification", vars);
        if (emailBody == null) {
            LOGGER.error("Email template failed for deadline notification, please configure the backend properly...");
        } else {
            Email email = new Email();
            email.setSender(smtpConfig.getUsername());
            email.setSubject("Notificare termene de rezolvare a petițiilor");
            email.setRecipients(user.getEmail());
            email.setBody(emailBody);
            try {
                LOGGER.info("Sending deadline notification email to user = " + user.getEmail() + " body = " + emailBody);
                smtpService.send(email);
            } catch (MessagingException e) {
                LOGGER.error("Could not send deadline notification for user = " + user.getEmail() + " Reason: " + e.getMessage());
            }
        }
    }
}
