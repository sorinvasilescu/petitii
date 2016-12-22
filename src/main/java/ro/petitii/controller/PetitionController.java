package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.Petitioner;
import ro.petitii.service.EmailService;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;

import javax.validation.Valid;
import java.util.Date;

@Controller
public class PetitionController extends ControllerBase {

    @Autowired
    UserService userService;

    @Autowired
    PetitionService petitionService;

    @Autowired
    EmailService emailService;

    @Autowired
    DefaultsConfig defaultsConfig;

    @RequestMapping(path = "/petition", method = RequestMethod.GET)
    public ModelAndView addPetition() {
        Petitioner petitioner = new Petitioner();
        petitioner.setCountry(defaultsConfig.getCountry());

        Petition petition = new Petition();
        petition.setReceivedDate(new Date());
        petition.setPetitioner(petitioner);

        ModelAndView modelAndView = new ModelAndView("petitions_crud");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping(path = "/petition/{id}", method = RequestMethod.GET)
    public ModelAndView editPetition(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("petitions_crud");

        Petition petition = petitionService.findById(id);
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());
        modelAndView.addObject("commentsApiUrl", "/api/petitions/" + petition.getId() + "/comments");
        modelAndView.addObject("attachmentApiUrl", "/api/petitions/" + petition.getId() + "/attachments");

        return modelAndView;
    }

    @RequestMapping(path = "/petition/fromEmail/{id}", method = RequestMethod.GET)
    public ModelAndView createPetitionFromEmail(@PathVariable("id") Long id) {
        Email email = emailService.searchById(id);

        Petition petition = petitionService.createFromEmail(email);

        ModelAndView modelAndView = new ModelAndView("petitions_crud");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("user_list", userService.getAllUsers());

        return modelAndView;
    }

    @RequestMapping(path = "/petition", method = RequestMethod.POST)
    public ModelAndView savePetition(@Valid Petition petition, BindingResult bindingResult, final RedirectAttributes attr) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("petitions_crud");
            modelAndView.addObject("user_list", userService.getAllUsers());
            modelAndView.addObject("petition", petition);
            modelAndView.addObject("toast", createToast("Petitia nu a fost salvata", ToastType.danger));
        } else {
            petition = petitionService.save(petition);
            modelAndView.setViewName("redirect:/petition/" + petition.getId());
            attr.addFlashAttribute("toast", createToast("Petitia a fost salvata cu succes", ToastType.success));
        }
        return modelAndView;
    }

    @RequestMapping("/petitions")
    public ModelAndView listUserPetitions() {
        ModelAndView modelAndView = new ModelAndView("petitions_list");
        modelAndView.addObject("apiUrl", "/api/petitions/user");
        return modelAndView;
    }

    @RequestMapping("/petitions/all")
    public ModelAndView listAllPetitions() {
        ModelAndView modelAndView = new ModelAndView("petitions_list");
        modelAndView.addObject("apiUrl", "/api/petitions/all");
        return modelAndView;
    }

    @RequestMapping("/redirect")
    public String redirectPetition() {
        return "petitions_redirect";
    }
}