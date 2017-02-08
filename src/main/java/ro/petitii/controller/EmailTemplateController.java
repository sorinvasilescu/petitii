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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.model.EmailTemplate;
import ro.petitii.service.EmailTemplateService;

import javax.validation.Valid;

import static ro.petitii.validation.ValidationUtil.*;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class EmailTemplateController extends ViewController {
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateController.class);
    @Autowired
    private EmailTemplateService emailTemplateService;

    @RequestMapping("/emailTemplates")
    public ModelAndView emailTemplates() {
        ModelAndView modelAndView = new ModelAndView("email_templates_list");
        modelAndView.addObject("apiUrl", "/api/emailTemplates");
        return modelAndView;
    }

    @RequestMapping(path = "/emailTemplate", method = RequestMethod.GET)
    public ModelAndView addEmailTemplate() {
        return editEmailTemplate(new EmailTemplate());
    }

    @RequestMapping(path = "/emailTemplate/{id}", method = RequestMethod.GET)
    public ModelAndView editEmailTemplate(@PathVariable("id") Long id) {
        EmailTemplate emailTemplate = emailTemplateService.findOne(id);
        check(assertNotNull(emailTemplate, i18n("controller.emailTemplate.invalid_id")), logger, redirect("emailTemplates"));
        return editEmailTemplate(emailTemplate);
    }

    @RequestMapping(path = "/emailTemplate/{id}/delete", method = RequestMethod.GET)
    public ModelAndView deleteEmailTemplates(@PathVariable("id") Long id, final RedirectAttributes attr) {
        check(assertTrue(emailTemplateService.delete(id), i18n("controller.emailTemplate.invalid_id")), logger, redirect("emailTemplates"));
        return successAndRedirect(i18n("controller.emailTemplate.delete.success"), "emailTemplates", attr);
    }

    @RequestMapping(path = "/emailTemplate", method = RequestMethod.POST)
    public ModelAndView saveEmailTemplate(@Valid EmailTemplate emailTemplate, final RedirectAttributes attr) {
        check(failOnException(() -> emailTemplateService.save(emailTemplate), i18n("controller.emailTemplate.save.fail")), logger, editEmailTemplate(emailTemplate));
        return successAndRedirect(i18n("controller.emailTemplate.save.success"), "emailTemplates", attr);
    }

    private ModelAndView editEmailTemplate(EmailTemplate template) {
        ModelAndView modelAndView = new ModelAndView("email_templates_crud");
        modelAndView.addObject("emailTemplate", template);
        modelAndView.addObject("categories", EmailTemplate.Category.values());
        return modelAndView;
    }
}
