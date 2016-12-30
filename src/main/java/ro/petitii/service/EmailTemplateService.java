package ro.petitii.service;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.EmailTemplate;

/**
 * Created by mpostelnicu on 12/28/2016.
 */
public interface EmailTemplateService {

    EmailTemplate findOne(Long aLong);

    EmailTemplate save(EmailTemplate emailTemplate);

    DataTablesOutput<EmailTemplate> findAll(DataTablesInput input);
}
