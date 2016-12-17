package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.Petitioner;
import ro.petitii.service.EmailService;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;
import ro.petitii.service.email.ImapService;

import java.util.Date;

@Controller
public class PetitionController extends ControllerBase {

    @Autowired
    UserService userService;

    @Autowired
    PetitionService petitionService;

    @Autowired
    EmailService emailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);

    @RequestMapping(path = "/petition", method = RequestMethod.GET)
    public ModelAndView addPetition() {
        Petitioner petitioner = new Petitioner();
        petitioner.setCountry("RO");

        Petition petition = new Petition();
        petition.setReceivedDate(new Date());
        petition.setPetitioner(petitioner);

        ModelAndView modelAndView = new ModelAndView("add_petition");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping(path = "/petition/{id}", method = RequestMethod.GET)
    public ModelAndView editPetition(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("add_petition");

        Petition petition = petitionService.findById(id);
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping(path = "/petition/fromEmail/{id}", method = RequestMethod.GET)
    public ModelAndView createPetitionFromEmail(@PathVariable("id") Long id) {
        Email email = emailService.searchById(id);

        Petition petition = new Petition();
        petition.setReceivedDate(new Date());
        petition.setDescription(email.getBody());
        petition.set_abstract(email.getSubject());

        //todo; move petitioner details

        ModelAndView modelAndView = new ModelAndView("add_petition");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping(path = "/petition", method = RequestMethod.POST)
    public ModelAndView savePetition(Petition petition) {
        LOGGER.info(petition.toString());
        petition = petitionService.save(petition);

        ModelAndView modelAndView = new ModelAndView("add_petition");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return createToast(modelAndView, "Petitie salvata cu succes", ToastType.success);
    }

    @RequestMapping("/petitii")
    public ModelAndView listUserPetitions() {
        ModelAndView modelAndView = new ModelAndView("petitii_page");
        modelAndView.addObject("restUrl", "/rest/petitions");
        return modelAndView;
    }

    @RequestMapping("/petitii/toate")
    public ModelAndView listAllPetitions() {
        ModelAndView modelAndView = new ModelAndView("petitii_page");
        modelAndView.addObject("restUrl", "/rest/petitions/all");
        return modelAndView;
    }

    @RequestMapping("/redirectionare")
    public String redirectPetition() {
        return "redirectioneaza_petition_page";
    }
}