package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PetitionsController {

    @RequestMapping("/petitii")
    public String petitii() {
        return "petitii_page";
    }
}
