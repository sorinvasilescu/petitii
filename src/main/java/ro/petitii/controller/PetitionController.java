package ro.petitii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.*;
import ro.petitii.service.*;
import ro.petitii.service.email.SmtpService;
import ro.petitii.util.DateUtil;
import ro.petitii.util.ValidationStatus;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private CommentService commentService;

    @Autowired
    private PetitionStatusService statusService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private SmtpService smtpService;

    @Autowired
    private SmtpConfig smtpConfig;

    @RequestMapping(path = "/petition", method = RequestMethod.GET)
    public ModelAndView addPetition() {
        Petitioner petitioner = new Petitioner();
        petitioner.setCountry(defaultsConfig.getCountry());

        Petition petition = new Petition();
        petition.setReceivedDate(new Date());
        petition.setDeadline(DateUtil.deadline(new Date()));
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
        modelAndView.addObject("petition", petition);
        List<Contact> contactList = (List<Contact>) (contactService.getAllContacts());
        modelAndView.addObject("contacts", contactList);
        return modelAndView;
    }

    @RequestMapping(value = "/petition/redirect/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView redirectPetition(@PathVariable("id") long id,
                                         @RequestParam("subject") String subject,
                                         @RequestParam("recipients") long[] recipients,
                                         @RequestParam(value = "attachments[]", required = false) long[] attachments,
                                         @RequestParam("description") String description,
                                         final RedirectAttributes attr) {

        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        Petition petition = petitionService.findById(id);
        modelAndView.setViewName("redirect:/petition/" + petition.getId());
        String recipientString = "";
        for (long rid : recipients) {
            Contact contact = contactService.getById(rid);
            recipientString += contact.getName() + " <" + contact.getEmail() + ">, ";
        }
        List<Attachment> attachmentList = new LinkedList<>();
        if (attachments != null)
            for (long aid : attachments) {
                attachmentList.add(attachmentService.findById(aid));
            }

        statusService.create(PetitionStatus.Status.REDIRECTED, petition, user);
        Email email = new Email();
        email.setBody(description);
        email.setDate(new Date());
        email.setSubject(subject);
        email.setSender(smtpConfig.getUsername());
        email.setRecipients(recipientString);
        email.setAttachments(attachmentList);
        email.setPetition(petition);
        email.setType(Email.EmailType.Outbox);
        email = emailService.save(email);

        try {
            smtpService.send(email);
            attr.addFlashAttribute("toast", createToast("Petiția a fost redirecționata cu succes", ToastType.success));
        } catch (MessagingException e) {
            attr.addFlashAttribute("toast", createToast("Petiția nu a fost redirecționata: " + e.getMessage(), ToastType.danger));
        }

        return modelAndView;
    }

    @RequestMapping(value = "/petition/{pid}/resolve/{action}", method = RequestMethod.GET)
    public ModelAndView resolve(@PathVariable("pid") Long pid, @PathVariable("action") String action,
                                final RedirectAttributes attr) {
        Petition petition = petitionService.findById(pid);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        if (petition.getCurrentStatus() == PetitionStatus.Status.IN_PROGRESS) {
            ModelAndView modelAndView = new ModelAndView("petitions_resolve");
            modelAndView.addObject("action", action);
            modelAndView.addObject("pid", pid);
            modelAndView.addObject("pEmail", petition.getPetitioner().getEmail());
            modelAndView.addObject("attachmentApiUrl", "/api/petitions/" + pid + "/attachments");
            modelAndView.addObject("linkedPetitionsApiUrl", "/api/petitions/" + pid + "/linked");
            modelAndView.addObject("linkedPetitionerApiUrl", "/api/petitions/" + pid + "/by/petitioner");
            return modelAndView;
        } else {
            ModelAndView modelAndView = new ModelAndView("redirect:/petition/" + petition.getId());
            attr.addFlashAttribute("toast", createToast("Doar petițiile în lucru se pot rezolva", ToastType.danger));
            return modelAndView;
        }
    }

    @RequestMapping(value = "/petition/{pid}/resolve/{action}", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView resolvePetition(@PathVariable("pid") long id,
                                        @PathVariable("action") String action,
                                        @RequestParam("resolution") PetitionStatus.Resolution resolution,
                                        @RequestParam("email") boolean sendEmail,
                                        @RequestParam(value = "attachments[]", required = false) long[] attachments,
                                        @RequestParam("description") String description,
                                        final RedirectAttributes attr) {

        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        Petition petition = petitionService.findById(id);
        modelAndView.setViewName("redirect:/petition/" + petition.getId());

        ValidationStatus validationStatus = validateSolutionParameters(petition, resolution, sendEmail, description);

        if (!validationStatus.isValid()) {
            attr.addFlashAttribute("toast", createToast(validationStatus.getMsg(), ToastType.danger));
            modelAndView.setViewName("redirect:/petition/" + id + "/resolve/" + action);
        } else {
            PetitionStatus.Status status = resolution.getStatus();

            statusService.create(status, petition, user);

            if (sendEmail) {
                List<Attachment> attachmentList = new LinkedList<>();
                if (attachments != null) {
                    for (long aid : attachments) {
                        attachmentList.add(attachmentService.findById(aid));
                    }
                }

                Email email = new Email();
                email.setBody(description);
                email.setDate(new Date());
                //todo; translate resolution in something more user friendly
                email.setSubject("Soluționare petiție: " + resolution);
                email.setSender(smtpConfig.getUsername());
                email.setRecipients(petition.getPetitioner().getEmail());
                email.setAttachments(attachmentList);
                email.setPetition(petition);
                email.setType(Email.EmailType.Outbox);
                email = emailService.save(email);

                try {
                    smtpService.send(email);
                    attr.addFlashAttribute("toast", createToast("Petiția a fost rezolvata cu succes", ToastType.success));
                } catch (MessagingException e) {
                    attr.addFlashAttribute("toast", createToast("Petiția nu a fost rezolvata: " + e.getMessage(), ToastType.danger));
                }
            } else {
                //todo; translate resolution in something more user friendly
                commentService.createAndSave(user, petition, "Soluționare petiție: " + resolution + " \n <br/> " + description);
                attr.addFlashAttribute("toast", createToast("Petiția a fost rezolvata cu succes", ToastType.success));
            }
        }

        return modelAndView;
    }

    private void addCustomParams(ModelAndView modelAndView) {
        modelAndView.addObject("user_list", userService.getAllUsers());

        for (PetitionCustomParam.Type type : PetitionCustomParam.Type.values()) {
            PetitionCustomParam param = petitionCustomParamService.findByType(type);
            modelAndView.addObject(type.name(), param);
        }
    }

    private ValidationStatus validateSolutionParameters(Petition petition, PetitionStatus.Resolution resolution,
                                                        boolean sendEmail, String description) {
        if (petition.getCurrentStatus() != PetitionStatus.Status.IN_PROGRESS) {
            return new ValidationStatus(false, "Doar petițiile în lucru se pot rezolva");
        }

        if (sendEmail && (description == null || description.trim().isEmpty())) {
            return new ValidationStatus(false, "Pentru a trimite o soluție petentului precizați un mesaj");
        }

        if (Objects.equals(resolution, PetitionStatus.Resolution.duplicate) && petitionService.countLinkedPetitions(petition) == 0) {
            return new ValidationStatus(false, "Pentru a închide o petiție duplicat precizați cel puțin o petiție conexată");
        }

        return new ValidationStatus(true, null);
    }
}