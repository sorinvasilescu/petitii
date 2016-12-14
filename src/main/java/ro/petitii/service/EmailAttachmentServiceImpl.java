package ro.petitii.service;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.config.EmailAttachmentConfig;
import ro.petitii.model.EmailAttachment;
import ro.petitii.repository.EmailAttachmentRepository;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.*;

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
        BodyPart attBody = a.getBodyPart();
        try {
            LOGGER.info("Getting attachment");
            a.setOriginalFilename(attBody.getFileName());
        } catch (MessagingException e2) {
            LOGGER.info("Could not parse message: " + e2.getMessage());
        }
        em.persist(a);
        em.flush();
        String extension = FilenameUtils.getExtension(a.getOriginalFilename());
        try {
            String filename = FilenameUtils.concat(config.getPath(),a.getId() + "." + extension);
            //((MimeBodyPart) attBody).saveFile(filename);
            BufferedInputStream in = new BufferedInputStream(attBody.getInputStream());
            OutputStream os = new FileOutputStream(filename);
            BufferedOutputStream out = new BufferedOutputStream(os);
            byte[] chunk = new byte[500000];
            int available;
            while ((available = in.read(chunk))!=-1) {
                out.write(chunk,0,available);
            }
            out.close();
            os.close();
            LOGGER.info("Saved file to disk");
            a.setFilename(filename);
            a.setContentType(attBody.getContentType());
        } catch (IOException e1) {
            LOGGER.info("Could not save file: " + e1.getMessage());
        } catch (MessagingException e2) {
            LOGGER.info("Could not parse message: " + e2.getMessage());
        }
        return emailAttachmentRepository.save(a);
    }

    @Override
    public EmailAttachment findById(Long id) {
        return emailAttachmentRepository.findOne(id);
    }

    private void prepFolder() {
        LOGGER.info("Preparing to save attachment in folder: " + config.getPath());
        File target = new File(config.getPath());
        if (!target.isDirectory()) {
            LOGGER.info("Creating directory structure: " + config.getPath());
            target.mkdirs();
        }
    }
}