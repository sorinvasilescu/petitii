package ro.petitii.service;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.petitii.config.EmailAttachmentConfig;
import ro.petitii.model.Attachment;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.datatables.AttachmentResponse;
import ro.petitii.repository.AttachmentRepository;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private EmailAttachmentConfig config;

    @Autowired
    private PetitionService petitionService;

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Attachment saveFromEmail(Attachment a) {
        LOGGER.info("Email id: " + a.getEmails().iterator().next().getId());
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
            ((MimeBodyPart) attBody).saveFile(filename);
            /*BufferedInputStream in = new BufferedInputStream(attBody.getInputStream());
            OutputStream os = new FileOutputStream(filename);
            BufferedOutputStream out = new BufferedOutputStream(os);
            byte[] chunk = new byte[500000];
            int available;
            while ((available = in.read(chunk)) != -1) {
                out.write(chunk, 0, available);
            }
            out.close();
            os.close();*/
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
    @Transactional
    public List<Attachment> saveFromForm(MultipartFile[] files, Long petitionId) {
        List<Attachment> attachments = new ArrayList<>();
        Attachment att;
        prepFolder();
        for (MultipartFile file : files) {
            att = new Attachment();
            att.setDate(new Date());
            em.persist(att);
            em.flush();
            Path filePath = Paths.get(config.getPath(), att.getId() + "." + FilenameUtils.getExtension(file.getOriginalFilename()));
            try {
                Files.copy(file.getInputStream(), filePath);
                att.setFilename(filePath.toString());
                att.setOriginalFilename(file.getOriginalFilename());
                att.setPetition(petitionService.findById(petitionId));
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByEmail(auth.getName()).get(0);
                att.setUser(user);

                attachmentRepository.save(att);
                LOGGER.info("Saved file " + file.getOriginalFilename() + " to: " + filePath.toString());
            } catch (IOException e) {
                LOGGER.error("Could not save attachment");
            }
        }
        return attachments;
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
    public List<Attachment> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            return ids.stream().map(this::findById).collect(Collectors.toList());
        }
    }

    @Override
    public List<Attachment> findByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(ids).map(this::findById).collect(Collectors.toList());
        }
    }

    @Override
    public void deleteFromPetition(long attachmentId) {
        Attachment att = attachmentRepository.findOne(attachmentId);
        if (att == null) return;
        att.setPetition(null);
        attachmentRepository.save(att);
        this.deleteFromDisk(att);
    }

    @Override
    public void deleteFromEmail(long attachmentId, long emailId) {
        Attachment att = attachmentRepository.findOne(attachmentId);
        if (att == null) return;
        List<Email> emails = att.getEmails();
        // if attachment has only one email
        if (emails.size() < 2) {
            // if attachment has no petition
            if (att.getPetition()==null) {
                // remove attachment entirely
                this.deleteFromDisk(att);
            } else {
                // remove email from attachment, but keep attachment
                att.setEmails(null);
                attachmentRepository.save(att);
            }
        } else {
            for (Email e : emails) {
                // remove current email from attachment
                if (e.getId()==emailId) {
                    e.getAttachments().remove(att);
                    emails.remove(e);
                    attachmentRepository.save(att);
                }
            }
        }
    }

    @Override
    public void deleteFromDisk(Attachment att) {
        if (att == null) return;
        // check if there are no references to the attachment
        if ((att.getPetition() == null) && (att.getEmails().size() < 1)) {
            // delete file
            File file = new File(att.getFilename());
            if (!file.delete()) {
                LOGGER.error("Could not delete file: " + file.getAbsolutePath());
            }
            // delete from db
            attachmentRepository.delete(att);
        }
    }

    @Override
    public DataTablesOutput<AttachmentResponse> getTableContent(Petition petition, PageRequest pageRequest) {
        Page<Attachment> attachments = attachmentRepository.findByPetition_Id(petition.getId(), pageRequest);

        List<AttachmentResponse> data = new LinkedList<>();
        for (Attachment e : attachments.getContent()) {
            AttachmentResponse re = new AttachmentResponse();
            re.setId(e.getId());
            re.setPetitionId(e.getPetition().getId());
            re.setFilename(e.getOriginalFilename());
            if (e.getEmails().size() < 1) {
                re.setOrigin(e.getUser().getFullName());
            } else {
                re.setOrigin("E-mail");
            }
            re.setDate(df.format(e.getDate()));
            data.add(re);
        }
        DataTablesOutput<AttachmentResponse> response = new DataTablesOutput<>();
        response.setData(data);
        Long count = attachmentRepository.countByPetitionId(petition.getId());
        response.setRecordsFiltered(count);
        response.setRecordsTotal(count);
        return response;
    }

    private void prepFolder() {
        LOGGER.info("Preparing to save attachment in folder: " + config.getPath());
        File target = new File(config.getPath());
        if (!target.isDirectory()) {
            LOGGER.info("Creating directory structure: " + config.getPath());
            if (!target.mkdirs()) {
                LOGGER.error("Failed to create directory structure: " + config.getPath());
            }
        }
    }
}