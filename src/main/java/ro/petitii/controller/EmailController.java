package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.config.ImapConfig;
import ro.petitii.model.Email;
import ro.petitii.service.EmailService;

@Controller
public class EmailController extends ControllerBase {
    @Autowired
    ImapConfig config;
    @Autowired
    EmailService emailService;

    @RequestMapping("/inbox")
    public ModelAndView inbox() {
        ModelAndView modelAndView = new ModelAndView("email_page");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", "Email-uri primite");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("restUrl", "/rest/emails");
        //modelAndView.addObject("toast",this.createToast("Lorem ipsum sit amet dolor",ToastType.info));

        return modelAndView;
    }

    @RequestMapping("/spam")
    public ModelAndView spam() {
        ModelAndView modelAndView = new ModelAndView("email_page");
        modelAndView.addObject("page", "spam");
        modelAndView.addObject("title", "Spam");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("restUrl", "/rest/spam");
        return modelAndView;
    }

    @RequestMapping("/emailDetails")
    public ModelAndView emailDetails(@RequestParam("id") Long id) {
        Email email = emailService.searchById(id);
        if (email == null) {
            throw new IllegalArgumentException("Invalid email id");
        }

        ModelAndView modelAndView = new ModelAndView("email_detail");
        modelAndView.addObject("page", "Detalii");
        modelAndView.addObject("title", "Email-uri primite");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("data", email);
        //todo; add a proper status, eventually as an enum
        String status = "";
        if (email.getPetition() != null) {
            status = "&#xce;n curs";
        } else if (email.getType() == Email.EmailType.Inbox) {
            status = "Nou";
        } else if (email.getType() == Email.EmailType.Outbox) {
            status = "Rezolvat";
        } else if (email.getType() == Email.EmailType.Spam) {
            status = "Spam";
        }
        modelAndView.addObject("status", status);
        return modelAndView;
    }
}
