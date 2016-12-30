package ro.petitii.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.Email;
import ro.petitii.model.datatables.EmailResponse;

import java.util.List;

public interface EmailService {
    Email save(Email e);
    long count();
    long count(Email.EmailType type);
    long lastUid();
    Email searchById(long emailId);
    List<Email> findAll(PageRequest p);
    List<Email> findAllByType(Email.EmailType type, PageRequest p);
    DataTablesOutput<EmailResponse> getTableContent(Email.EmailType type, PageRequest pageRequest);
}
