package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.config.PetitionIdConfig;
import ro.petitii.generator.AutoincrementGenerator;
import ro.petitii.generator.GenericGenerator;
import ro.petitii.model.Petition;
import ro.petitii.model.RegistrationNo;
import ro.petitii.service.UserService;

import javax.inject.Inject;
import java.util.Date;

@Controller
public class PetitionController {
    private GenericGenerator generator;
    private UserService userService;

    @Inject
    public PetitionController(PetitionIdConfig petitionIdConfig, UserService userService) {
        //todo; DB lookup for the last id
        generator = new AutoincrementGenerator(petitionIdConfig.getPattern(), 0);
        this.userService = userService;
    }

    @RequestMapping("/addPetition")
    public ModelAndView addPetition() {
        RegistrationNo registrationNo = new RegistrationNo();
        registrationNo.setNumber(generator.generateId());

        Petition petition = new Petition();
        petition.setRegNo(registrationNo);
        petition.setReceivedDate(new Date());


        ModelAndView modelAndView = new ModelAndView("add_petition");
        modelAndView.addObject("data", petition);
        modelAndView.addObject("user_options", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping("/petitii")
    public String listPetitions() {
        return "petitii_page";
    }
}