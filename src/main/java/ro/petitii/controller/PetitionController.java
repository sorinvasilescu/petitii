package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.model.Petition;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);

    @RequestMapping(path = "/petition", method = RequestMethod.GET)
    public ModelAndView addPetition() {
        Petition petition = new Petition();
        petition.setReceivedDate(new Date());

        ModelAndView modelAndView = new ModelAndView("add_petition");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping(path = "/petition/{id}", method = RequestMethod.GET)
    public ModelAndView editPetition(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("add_petition");

        Petition petition = petitionService.findById(id);
        modelAndView.addObject("petition",petition);

        return modelAndView;
    }

    @RequestMapping(path = "/petition", method = RequestMethod.POST)
    public ModelAndView savePetition(Petition petition) {
        LOGGER.info(petition.toString());
        petitionService.save(petition);

        ModelAndView modelAndView = new ModelAndView("add_petition");
        modelAndView.addObject("petition", petition);
        createToast("Petitie salvata cu succes",ToastType.success);

        return modelAndView;
    }

    @RequestMapping("/petitii")
    public String listPetitions() {
        return "petitii_page";
    }

    @RequestMapping("/redirectionare")
    public String redirectPetition() {
        return "redirectioneaza_petition_page";
    }
}