package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ro.petitii.service.email.ImapService;

@Controller
public class MainController implements ErrorController {

    @Autowired
    ImapService imapService;

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

    @Override
    public String getErrorPath() {
        return "/error";
    }
}