package ro.petitii.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import ro.petitii.repository.EmailTemplateRepository;
import ro.petitii.service.template.EmailTemplateResolver;

import javax.annotation.PostConstruct;

/**
 * Created by mpostelnicu on 12/29/2016.
 */
@Configuration
public class ThymeleafEmailTemplateConfig {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @PostConstruct
    public void emailTemplateExtension() {
        EmailTemplateResolver emailTemplateResolver= new EmailTemplateResolver(emailTemplateRepository);
        templateEngine.addTemplateResolver(emailTemplateResolver);
    }
}