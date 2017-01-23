package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.config.ImapConfig;
import ro.petitii.model.Email;
import ro.petitii.service.EmailService;
import ro.petitii.validation.ValidationUtil;

import javax.servlet.http.HttpServletRequest;

import static ro.petitii.validation.ValidationUtil.redirect;

@Controller
public class EmailController extends ViewController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    private static final ValidationUtil v = new ValidationUtil(logger);

    @Autowired
    private ImapConfig config;

    @Autowired
    private EmailService emailService;

    @RequestMapping("/inbox")
    public ModelAndView inbox() {
        ModelAndView modelAndView = new ModelAndView("email_list");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", i18n("controller.email.received_emails"));
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("apiUrl", "/api/emails");
        return modelAndView;
    }

    @RequestMapping("/spam")
    public ModelAndView spam() {
        ModelAndView modelAndView = new ModelAndView("email_list");
        modelAndView.addObject("page", "spam");
        modelAndView.addObject("title", i18n("controller.email.spam"));
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("apiUrl", "/api/spam");
        return modelAndView;
    }

    @RequestMapping("/email/{id}")
    public ModelAndView emailDetails(@PathVariable("id") Long id, HttpServletRequest request) {
        Email email = emailService.searchById(id);

        String referer = request.getHeader("referer");
        String path = "";
        try {
            path = referer.split("//")[1].substring(referer.split("//")[1].indexOf("/") + 1);
        } catch (NullPointerException e) {
            // no biggie, the visit was by typing the url directly
        }
        if (path.length() < 1) path = "inbox";
        v.failIfNull(email, i18n("controller.email.invalid_id"), redirect(path));

        ModelAndView modelAndView = new ModelAndView("email_detail");
        modelAndView.addObject("title", i18n("controller.email.received_emails"));
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("data", email);
        if (email.getPetition() != null) {
            modelAndView.addObject("status", i18n(email.getPetition().statusString()));
        }
        return modelAndView;
    }
}
