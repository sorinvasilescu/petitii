package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import ro.petitii.model.EmailTemplate;
import ro.petitii.repository.EmailTemplateRepository;

/**
 * Created by mpostelnicu on 12/28/2016.
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Override
    public EmailTemplate findOne(Long aLong) {
        return emailTemplateRepository.findOne(aLong);
    }

    @Override
    public EmailTemplate save(EmailTemplate emailTemplate) {
        return emailTemplateRepository.save(emailTemplate);
    }

    @Override
    public DataTablesOutput<EmailTemplate> findAll(DataTablesInput input) {
        return emailTemplateRepository.findAll(input);
    }
}
