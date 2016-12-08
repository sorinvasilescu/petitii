package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class EmailController {

    @RequestMapping("/inbox")
    public String inbox() {
        return "email_page";
    }

    @RequestMapping("/spam")
    public String spam() {
        return "spam_page";
    }
}
