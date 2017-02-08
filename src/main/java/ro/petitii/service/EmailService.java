package ro.petitii.service;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.datatables.EmailResponse;

public interface EmailService {
    Email save(Email e);
    long count();
    long lastUid();
    Email searchById(long emailId);
    DataTablesOutput<EmailResponse> getTableContent(DataTablesInput input, Email.EmailType type);
    DataTablesOutput<EmailResponse> getTableContent(DataTablesInput input, Petition petition);
}
