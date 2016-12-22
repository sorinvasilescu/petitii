package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import ro.petitii.model.Email;
import ro.petitii.model.Attachment;
import ro.petitii.model.dt.DTEmailResponseElement;
import ro.petitii.repository.EmailRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private AttachmentService attachmentService;

    @PersistenceContext
    EntityManager em;

    @Override
    @Transactional
    public Email save(Email e) {
        em.persist(e);
        em.flush();
        for (Attachment attachment : e.getAttachments()) {
            attachment.setEmail(e);
            attachmentService.saveFromEmail(attachment);
        }
        e = emailRepository.save(e);
        return e;
    }

    @Override
    public Email saveAlone(Email e) {
        return emailRepository.save(e);
    }

    @Override
    public long count() {
        return emailRepository.count();
    }

    @Override
    public long count(Email.EmailType type) {
        return emailRepository.countByType(type);
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
    public List<Email> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortcolumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortcolumn);
        Page<Email> emails = emailRepository.findAll(p);
        return emails.getContent();
    }

    @Override
    public List<Email> findAllByType(Email.EmailType type, int startIndex, int size, Sort.Direction sortDirection, String sortcolumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortcolumn);
        Page<Email> emails = emailRepository.findByType(type, p);
        return emails.getContent();
    }

    @Override
    public DataTablesOutput<DTEmailResponseElement> getTableContent(Email.EmailType type, int startIndex, int size, Sort.Direction sortDirection, String sortColumn) {
        List<Email> result = this.findAllByType(type, startIndex, size, sortDirection, sortColumn);
        List<DTEmailResponseElement> data = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Email e : result) {
            DTEmailResponseElement re = new DTEmailResponseElement();
            re.setId(e.getId());
            re.setSender(e.getSender());
            re.setSubject(e.getSubject());
            re.setDate(df.format(e.getDate()));
            if (e.getPetition() != null) re.setPetition_id(e.getPetition().getId());
            data.add(re);
        }
        DataTablesOutput<DTEmailResponseElement> response = new DataTablesOutput<>();
        response.setData(data);
        Long count = this.count(type);
        response.setRecordsFiltered(count);
        response.setRecordsTotal(count);
        return response;
    }
}
