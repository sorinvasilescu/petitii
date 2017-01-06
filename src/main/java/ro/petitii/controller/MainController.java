package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.petitii.model.Email;
import ro.petitii.service.email.SmtpService;

import javax.mail.MessagingException;

@Controller
public class MainController implements ErrorController {
    @Autowired
    private SmtpService smtpService;

    @RequestMapping("/error")
    public String error() {
        return "error";
    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/inbox";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/editor-toolbar")
    public String editorToolbar() {
        return "fragments/editor-toolbar";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/smtptest")
    @ResponseBody
    public String smtpTest() {
        Email email = new Email();
        email.setRecipients("sorin.vasilescu@gmail.com");
        email.setSubject("Test");
        email.setBody("Lorem ipsum");
        email.setSender("petitii.gov@gmail.com");
        try {
            smtpService.send(email);
        } catch (MessagingException e) {
            return e.getMessage();
        }
        return "Done";
    }
}