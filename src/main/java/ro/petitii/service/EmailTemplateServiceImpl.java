package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import ro.petitii.model.EmailTemplate;
import ro.petitii.repository.EmailTemplateRepository;

import java.util.List;

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

    @Override
    public List<EmailTemplate> findByCategory(EmailTemplate.Category category) {
        return emailTemplateRepository.findByCategory(category);
    }

    @Override
    public EmailTemplate findOneByCategory(EmailTemplate.Category category) {
        List<EmailTemplate> templates = emailTemplateRepository.findByCategory(category);
        if (templates.isEmpty()) {
            return null;
        } else {
            return templates.get(0);
        }
    }

    @Override
    public void delete(long id) {
        emailTemplateRepository.delete(id);
    }
}
