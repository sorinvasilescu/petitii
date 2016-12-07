package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ro.petitii.model.Email;
import ro.petitii.model.EmailAttachment;
import ro.petitii.repository.EmailRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EmailAttachmentService attachmentService;

    @PersistenceContext
    EntityManager em;

    @Override
    @Transactional
    public Email save(Email e) {
        em.persist(e);
        em.flush();
        for (EmailAttachment attachment : e.getAttachments()) {
            attachment.setEmail(e);
            attachmentService.save(attachment);
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
        PageRequest pr = new PageRequest(0,1, Sort.Direction.DESC, "uid");
        Page<Email> result = emailRepository.findAll(pr);
        if (result.getSize()>0) return result.getContent().get(0).getUid();
        else return -1;
    }
}
