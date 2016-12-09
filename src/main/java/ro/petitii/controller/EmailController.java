package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ro.petitii.config.ImapConfig;

@Controller
public class EmailController {
    
    @Autowired
    ImapConfig config;

    @RequestMapping("/inbox")
    public ModelAndView inbox() {
        ModelAndView modelAndView = new ModelAndView("email_page");
        modelAndView.addObject("title","Email-uri primite");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("restUrl","/rest/emails");
        return modelAndView;
    }

    @RequestMapping("/spam")
    public ModelAndView spam() {
        ModelAndView modelAndView = new ModelAndView("email_page");
        modelAndView.addObject("title","Spam");
        modelAndView.addObject("email", config.getUsername());
        modelAndView.addObject("restUrl","/rest/spam");
        return modelAndView;
    }
}
