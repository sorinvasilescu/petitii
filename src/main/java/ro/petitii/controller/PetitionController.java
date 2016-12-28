package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.model.*;
import ro.petitii.service.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Controller
public class PetitionController extends ControllerBase {
    @Autowired
    private UserService userService;

    @Autowired
    private PetitionService petitionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DefaultsConfig defaultsConfig;

    @Autowired
    private PetitionCustomParamService petitionCustomParamService;

    @Autowired
    private ContactService contactService;

    @RequestMapping(path = "/petition", method = RequestMethod.GET)
    public ModelAndView addPetition() {
        Petitioner petitioner = new Petitioner();
        petitioner.setCountry(defaultsConfig.getCountry());

        Petition petition = new Petition();
        petition.setReceivedDate(new Date());
        petition.setPetitioner(petitioner);

        petitionCustomParamService.initDefaults(petition);

        ModelAndView modelAndView = new ModelAndView("petitions_crud");
        modelAndView.addObject("petition", petition);

        addCustomParams(modelAndView);

        return modelAndView;
    }

    @RequestMapping(path = "/petition/{id}", method = RequestMethod.GET)
    public ModelAndView editPetition(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("petitions_crud");

        Petition petition = petitionService.findById(id);
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("commentsApiUrl", "/api/petitions/" + petition.getId() + "/comments");
        modelAndView.addObject("attachmentApiUrl", "/api/petitions/" + petition.getId() + "/attachments");
        modelAndView.addObject("linkedPetitionsApiUrl", "/api/petitions/" + petition.getId() + "/linked");
        modelAndView.addObject("linkedPetitionerApiUrl", "/api/petitions/" + petition.getId() + "/by/petitioner");

        addCustomParams(modelAndView);

        return modelAndView;
    }

    @RequestMapping(path = "/petition/fromEmail/{id}", method = RequestMethod.GET)
    public ModelAndView createPetitionFromEmail(@PathVariable("id") Long id) {
        Email email = emailService.searchById(id);

        Petition petition = petitionService.createFromEmail(email);
        petitionCustomParamService.initDefaults(petition);

        ModelAndView modelAndView = new ModelAndView("petitions_crud");
        modelAndView.addObject("petition", petition);

        addCustomParams(modelAndView);

        return modelAndView;
    }

    @RequestMapping(path = "/petition", method = RequestMethod.POST)
    public ModelAndView savePetition(@Valid Petition petition, BindingResult bindingResult,
                                     final RedirectAttributes attr) {
        ModelAndView modelAndView = new ModelAndView();

        petitionCustomParamService.validate(petition, bindingResult);

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("petitions_crud");
            addCustomParams(modelAndView);
            modelAndView.addObject("petition", petition);
            modelAndView.addObject("toast", createToast("Petiția nu a fost salvata", ToastType.danger));
        } else {
            petition = petitionService.save(petition);
            modelAndView.setViewName("redirect:/petition/" + petition.getId());
            attr.addFlashAttribute("toast", createToast("Petiția a fost salvata cu succes", ToastType.success));
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

    @RequestMapping(value = "/petition/redirect/{id}", method = RequestMethod.GET)
    public ModelAndView redirectPetition(@PathVariable("id") long id) {
        ModelAndView modelAndView = new ModelAndView("petitions_redirect");
        Petition petition = petitionService.findById(id);
        modelAndView.addObject("petition",petition);
        List<Contact> contactList = (List<Contact>)(contactService.getAllContacts());
        modelAndView.addObject("contacts",contactList);
        return modelAndView;
    }

    @RequestMapping(value = "/petition/redirect/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String redirectPetition(@PathVariable("id") long id,
                                         @RequestParam("subject") String subject,
                                         @RequestParam("recipients") long[] recipients,
                                         @RequestParam("attachments[]") long[] attachments,
                                         @RequestParam("description") String description) {
        String result = "";
        result += subject + '\n';
        result += recipients.toString() + '\n';
        result += attachments.toString() + '\n';
        result += description;
        return result;
    }

    private void addCustomParams(ModelAndView modelAndView) {
        modelAndView.addObject("user_list", userService.getAllUsers());

        for (PetitionCustomParam.Type type : PetitionCustomParam.Type.values()) {
            PetitionCustomParam param = petitionCustomParamService.findByType(type);
            modelAndView.addObject(type.name(), param);
        }
    }
}