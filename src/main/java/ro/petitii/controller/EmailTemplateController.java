package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.model.EmailTemplate;
import ro.petitii.service.EmailTemplateService;
import ro.petitii.service.email.ImapService;
import ro.petitii.service.template.EmailTemplateProcessorService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class EmailTemplateController extends ControllerBase {

    @Autowired
    EmailTemplateService emailTemplateService;

    @Autowired
    EmailTemplateProcessorService emailTemplateProcessorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);

    @RequestMapping("/emailTemplates")
    public ModelAndView emailTemplates() {
        ModelAndView modelAndView = new ModelAndView("email_templates_list");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", "Email Templates");
        modelAndView.addObject("apiUrl", "/api/emailTemplates");
        return modelAndView;
    }

    @RequestMapping(path = "/emailTemplate/{id}", method = RequestMethod.GET)
    public ModelAndView addEmailTemplate(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("email_templates_crud");

        EmailTemplate emailTemplate = emailTemplateService.findOne(id);
        modelAndView.addObject("emailTemplate", emailTemplate);

        return modelAndView;
    }

    @RequestMapping(path = "/emailTemplate", method = RequestMethod.GET)
    public ModelAndView addEmailTemplate() {
        EmailTemplate emailTemplate = new EmailTemplate();

        ModelAndView modelAndView = new ModelAndView("email_templates_crud");
        modelAndView.addObject("emailTemplate", emailTemplate);
        return modelAndView;
    }


    public void logTemplateExample(EmailTemplate emailTemplate) {
        Set<String> variables = emailTemplateProcessorService.extractVariables(emailTemplate);

        Map<String, Object> values=new HashMap<>();
        variables.forEach(v-> values.put(v,"--value of "+v+"--"));

        System.out.println(emailTemplateProcessorService.processTemplateWithId(emailTemplate.getId(),values));
    }

    @RequestMapping(path = "/emailTemplate", method = RequestMethod.POST)
    public ModelAndView saveEmailTemplate(@Valid EmailTemplate emailTemplate) {

        emailTemplateService.save(emailTemplate);

        //just for demonstration purposes
        logTemplateExample(emailTemplate);


        return new ModelAndView("redirect:/emailTemplates");
    }

}
