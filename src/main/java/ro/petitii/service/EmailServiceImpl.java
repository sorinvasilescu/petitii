package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.petitii.model.Attachment;
import ro.petitii.model.Email;
import ro.petitii.model.Email_;
import ro.petitii.model.Petition;
import ro.petitii.model.datatables.EmailConverter;
import ro.petitii.model.datatables.EmailResponse;
import ro.petitii.repository.EmailRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private AttachmentService attachmentService;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Email save(Email e) {
        if (e.getId() == null) {
            em.persist(e);
            em.flush();
        }
        for (Attachment attachment : e.getAttachments()) {
            List<Email> emails = attachment.getEmails();
            if (emails == null) emails = new ArrayList<>();
            if (!emails.contains(e)) {
                emails.add(e);
                attachment.setEmails(emails);
            }
            if (attachment.getBodyPart() != null) attachmentService.saveFromEmail(attachment);
            else attachmentService.save(attachment);
        }
        e = emailRepository.save(e);
        return e;
    }

    @Override
    public long count() {
        return emailRepository.count();
    }

    @Override
    public long lastUid() {
        PageRequest pr = new PageRequest(0, 1, Sort.Direction.DESC, "uid");
        Page<Email> result = emailRepository.findAll(pr);
        if (result.getSize() > 0) return result.getContent().get(0).getUid();
        else return -1;
    }

    @Override
    public Email searchById(long emailId) {
        return emailRepository.findOne(emailId);
    }

    @Override
    public DataTablesOutput<EmailResponse> getTableContent(DataTablesInput input, Email.EmailType type) {
        DataTablesOutput<EmailResponse> emails;
        Specification<Email> spec = null;

        if (type != null) {
            spec = (Root<Email> root, CriteriaQuery<?> q, CriteriaBuilder cb) -> cb.equal(root.get(Email_.type), type);
        }

        emails = emailRepository.findAll(input, null, spec, new EmailConverter());
        return emails;
    }

    @Override
    public DataTablesOutput<EmailResponse> getTableContent(DataTablesInput input, Petition petition) {
        DataTablesOutput<EmailResponse> emails;
        Specification<Email> spec = null;

        if (petition != null) {
            spec = (Root<Email> root, CriteriaQuery<?> q, CriteriaBuilder cb) -> cb
                    .equal(root.get(Email_.petition), petition);
        }

        emails = emailRepository.findAll(input, null, spec, new EmailConverter());
        return emails;
    }
}
