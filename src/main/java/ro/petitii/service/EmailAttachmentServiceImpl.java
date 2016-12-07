package ro.petitii.service;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.config.EmailAttachmentConfig;
import ro.petitii.config.ImapConfig;
import ro.petitii.model.EmailAttachment;
import ro.petitii.repository.EmailAttachmentRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;

@Service
public class EmailAttachmentServiceImpl implements EmailAttachmentService {

    @Autowired
    EmailAttachmentRepository emailAttachmentRepository;

    @Autowired
    EmailAttachmentConfig config;

    @PersistenceContext
    EntityManager em;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailAttachmentServiceImpl.class);

    @Override
    @Transactional
    public EmailAttachment save(EmailAttachment a) {
        LOGGER.info("Email id: " + a.getEmail().getId());
        prepFolder();
        try {
            a.setOriginalFilename(a.getBodyPart().getFileName());
        } catch (MessagingException e2) {
            LOGGER.info("Could not parse message:" + e2.getMessage());
        }
        em.persist(a);
        em.flush();
        String extension = FilenameUtils.getExtension(a.getOriginalFilename());
        try {
            String filename = FilenameUtils.concat(config.getPath(),a.getId() + "." + extension);
            ((MimeBodyPart) a.getBodyPart()).saveFile(filename);
            a.setFilename(filename);
            a.setContentType(a.getBodyPart().getContentType());
        } catch (IOException e1) {
            LOGGER.info("Could not save file:" + e1.getMessage());
        } catch (MessagingException e2) {
            LOGGER.info("Could not parse message:" + e2.getMessage());
        }
        return emailAttachmentRepository.save(a);
    }

    private void prepFolder() {
        LOGGER.info("Preparing to save attachment in folder:" + config.getPath());
        File target = new File(config.getPath());
        if (!target.isDirectory()) {
            LOGGER.info("Creating directory structure:" + config.getPath());
            target.mkdirs();
        }
    }
}