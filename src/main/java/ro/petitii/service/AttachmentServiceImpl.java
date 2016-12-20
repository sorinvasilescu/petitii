package ro.petitii.service;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ro.petitii.config.EmailAttachmentConfig;
import ro.petitii.model.Attachment;
import ro.petitii.model.Petition;
import ro.petitii.model.rest.RestAttachmentResponse;
import ro.petitii.model.rest.RestAttachmentResponseElement;
import ro.petitii.repository.AttachmentRepository;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private EmailAttachmentConfig config;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Attachment saveAndDownload(Attachment a) {
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
            String filename = FilenameUtils.concat(config.getPath(), a.getId() + "." + extension);
            //((MimeBodyPart) attBody).saveFile(filename);
            BufferedInputStream in = new BufferedInputStream(attBody.getInputStream());
            OutputStream os = new FileOutputStream(filename);
            BufferedOutputStream out = new BufferedOutputStream(os);
            byte[] chunk = new byte[500000];
            int available;
            while ((available = in.read(chunk)) != -1) {
                out.write(chunk, 0, available);
            }
            out.close();
            os.close();
            LOGGER.info("Saved file to disk");
            a.setFilename(filename);
            a.setContentType(attBody.getContentType());
        } catch (IOException e1) {
            LOGGER.info("Could not saveAndDownload file: " + e1.getMessage());
        } catch (MessagingException e2) {
            LOGGER.info("Could not parse message: " + e2.getMessage());
        }
        return attachmentRepository.save(a);
    }

    @Override
    public Attachment save(Attachment e) {
        return attachmentRepository.save(e);
    }

    @Override
    public Attachment findById(Long id) {
        return attachmentRepository.findOne(id);
    }

    @Override
    public void delete(long attachmentId) {
        //todo; delete from disk if the attachment is not part of an e-mail
        attachmentRepository.delete(attachmentId);
    }

    @Override
    public RestAttachmentResponse getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection, String sortColumn) {
        if (Objects.equals(sortColumn, "origin")) {
            sortColumn = "email";
        }

        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortColumn);
        Page<Attachment> attachments = attachmentRepository.findByPetitionId(petition.getId(), p);

        List<RestAttachmentResponseElement> data = new LinkedList<>();
        for (Attachment e : attachments.getContent()) {
            RestAttachmentResponseElement re = new RestAttachmentResponseElement();
            re.setId(e.getId());
            re.setPetitionId(e.getPetition().getId());
            re.setFilename(e.getOriginalFilename());
            if (e.getEmail() == null) {
                re.setOrigin(e.getUser().getFullName());
            } else {
                re.setOrigin("E-mail");
            }
            re.setDate(df.format(e.getDate()));
            data.add(re);
        }
        RestAttachmentResponse response = new RestAttachmentResponse();
        response.setData(data);
        Long count = attachmentRepository.countByPetitionId(petition.getId());
        response.setRecordsFiltered(count);
        response.setRecordsTotal(count);
        return response;
    }

    private void prepFolder() {
        LOGGER.info("Preparing to saveAndDownload attachment in folder: " + config.getPath());
        File target = new File(config.getPath());
        if (!target.isDirectory()) {
            LOGGER.info("Creating directory structure: " + config.getPath());
            if (!target.mkdirs()) {
                LOGGER.error("Failed to create directory structure: " + config.getPath());
            }
        }
    }
}