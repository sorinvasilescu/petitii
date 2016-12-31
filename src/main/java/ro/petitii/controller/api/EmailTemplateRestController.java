package ro.petitii.controller.api;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ro.petitii.model.EmailTemplate;
import ro.petitii.service.EmailTemplateService;

import javax.validation.Valid;

@RestController
public class EmailTemplateRestController {
    @Autowired
    private EmailTemplateService emailTemplateService;

    @JsonView(DataTablesOutput.View.class)
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/api/emailTemplates", method = {RequestMethod.GET, RequestMethod.POST})
    public DataTablesOutput<EmailTemplate> getEmailTemplates(@Valid DataTablesInput input) {
        return emailTemplateService.findAll(input);
    }
}