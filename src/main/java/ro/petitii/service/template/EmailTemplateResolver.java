package ro.petitii.service.template;

import com.google.common.collect.Sets;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import ro.petitii.model.EmailTemplate;
import ro.petitii.repository.EmailTemplateRepository;

import java.util.Map;

public class EmailTemplateResolver extends StringTemplateResolver {
    final static String PREFIX = "emailTemplate:";

    private EmailTemplateRepository templateRepository;

    public EmailTemplateResolver(EmailTemplateRepository templateRepository) {
        super();
        this.templateRepository = templateRepository;
        setResolvablePatterns(Sets.newHashSet(PREFIX + "*"));
    }


    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate,
                                                        String template,
                                                        Map<String, Object> templateResolutionAttributes) {
        Long templateId = Long.parseLong(template.substring(PREFIX.length()));
        EmailTemplate emailTemplate = templateRepository.findOne(templateId);
        if (emailTemplate == null) {
            throw new RuntimeException(String.format("EmailTemplate with id %d not found!", templateId));
        }
        return super.computeTemplateResource(configuration, ownerTemplate, emailTemplate.getContent(),
                                             templateResolutionAttributes);
    }

}