package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.config.ImapConfig;
import ro.petitii.model.Email;
import ro.petitii.service.EmailService;
import ro.petitii.util.TranslationUtil;

import java.util.Locale;

@Controller
public class EmailController extends ControllerBase {
    @Autowired
    private ImapConfig config;

    @Autowired
    private EmailService emailService;
    
    @Autowired
	private TranslationUtil translationService;
    
    @RequestMapping("/inbox")
    public ModelAndView inbox() {
        ModelAndView modelAndView = new ModelAndView("email_list");
        modelAndView.addObject("page", "inbox");
        String titleMessage = translationService.i18n("controller.email.received_emails");
        modelAndView.addObject("title", titleMessage);
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("apiUrl", "/api/emails");
        return modelAndView;
    }

    @RequestMapping("/spam")
    public ModelAndView spam() {
        ModelAndView modelAndView = new ModelAndView("email_list");
        modelAndView.addObject("page", "spam");
        String titleMessage = translationService.i18n("controller.email.spam");
        modelAndView.addObject("title", titleMessage);
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("apiUrl", "/api/spam");
        return modelAndView;
    }

    @RequestMapping("/email/{id}")
    public ModelAndView emailDetails(@PathVariable("id") Long id) {
        Email email = emailService.searchById(id);
        if (email == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        ModelAndView modelAndView = new ModelAndView("email_detail");
        //TODO: is the "page" really used?  (sergiu)
        modelAndView.addObject("page", "Detalii");
        String titleMessage = translationService.i18n("controller.email.received_emails");
        modelAndView.addObject("title", titleMessage);
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("data", email);
        if (email.getPetition() != null) {
            String status = translationService.i18n(email.getPetition().statusString());
            modelAndView.addObject("status", status);
        }
        return modelAndView;
    }
}
