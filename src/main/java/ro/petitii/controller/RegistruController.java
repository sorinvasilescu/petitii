package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RegistruController {

    @RequestMapping("/registru")
    public String listPetitions() {
        return "registru_page";
    }
}