package ro.petitii.service;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.EmailTemplate;

import java.util.List;

public interface EmailTemplateService {
    EmailTemplate findOne(Long aLong);

    EmailTemplate save(EmailTemplate emailTemplate);

    DataTablesOutput<EmailTemplate> findAll(DataTablesInput input);

    List<EmailTemplate> findByCategory(EmailTemplate.Category category);

    EmailTemplate findOneByCategory(EmailTemplate.Category category);

    boolean delete(long id);
}
