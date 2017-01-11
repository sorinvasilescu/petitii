package ro.petitii.tasks;

import org.slf4j.LoggerFactory;
import ro.petitii.service.email.ImapService;

import javax.mail.MessagingException;
import java.io.IOException;

public class EmailChecker implements Runnable {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmailChecker.class);

    private ImapService imapService;

    EmailChecker(ImapService imapService) {
        this.imapService = imapService;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Checking e-mail ...");
            imapService.getMail();
            LOGGER.info("Done checking e-mail ...");
        } catch (IOException | MessagingException e) {
            LOGGER.error("Failed to check e-mail: " + e.getMessage());
        }
    }
}
