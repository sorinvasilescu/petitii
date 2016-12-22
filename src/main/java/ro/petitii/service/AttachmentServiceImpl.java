package ro.petitii.service;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.petitii.config.EmailAttachmentConfig;
import ro.petitii.model.Attachment;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.dt.DTAttachmentResponse;
import ro.petitii.model.dt.DTAttachmentResponseElement;
import ro.petitii.repository.AttachmentRepository;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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
    public void deleteFromPetition(long attachmentId) {
        Attachment att = attachmentRepository.findOne(attachmentId);
        if (att == null) return;
        att.setPetition(null);
        attachmentRepository.save(att);
        this.deleteFromDisk(att);
    }

    @Override
    public void deleteFromEmail(long attachmentId) {
        Attachment att = attachmentRepository.findOne(attachmentId);
        if (att == null) return;
        att.setEmail(null);
        attachmentRepository.save(att);
        this.deleteFromDisk(att);
    }

    @Override
    public void deleteFromDisk(Attachment att) {
        if (att == null) return;
        // check if there are no references to the attachment
        if ( (att.getPetition() == null) && (att.getEmail() == null) ) {
            // delete file
            File file = new File(att.getFilename());
            file.delete();
            // delete from db
            attachmentRepository.delete(att);
        }
    }

    @Override
    public DTAttachmentResponse getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection, String sortColumn) {
        if (Objects.equals(sortColumn, "origin")) {
            sortColumn = "email";
        }

        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortColumn);
        Page<Attachment> attachments = attachmentRepository.findByPetitionId(petition.getId(), p);

        List<DTAttachmentResponseElement> data = new LinkedList<>();
        for (Attachment e : attachments.getContent()) {
            DTAttachmentResponseElement re = new DTAttachmentResponseElement();
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
        DTAttachmentResponse response = new DTAttachmentResponse();
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