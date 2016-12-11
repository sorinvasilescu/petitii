package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PetitionController {

    @RequestMapping("/addPetition")
    public String addPetition() {
        return "add_petition";
    }

    @RequestMapping("/petitii")
    public String listPetitions() {
        return "petitii_page";
    }
}