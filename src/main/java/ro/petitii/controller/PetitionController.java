package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.config.DeadlineConfig;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.*;
import ro.petitii.service.*;
import ro.petitii.service.email.SmtpService;
import ro.petitii.util.DateUtil;
import ro.petitii.util.StringUtil;
import ro.petitii.util.ToastMaster;
import ro.petitii.validation.ValidationStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static ro.petitii.validation.ValidationUtil.*;

@Controller
public class PetitionController extends ViewController {
    private static final Logger logger = LoggerFactory.getLogger(PetitionController.class);

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
    private EmailTemplateService emailTemplateService;

    @Autowired
    private SmtpService smtpService;

    @Autowired
    private SmtpConfig smtpConfig;

    @Autowired
    private DeadlineConfig deadlineConfig;

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

    private ModelAndView editPetition(Petition petition) {
        ModelAndView modelAndView = new ModelAndView("petitions_crud");
        modelAndView.addObject("petition", petition);
        if (petition.getId() != null) {
            modelAndView.addObject("commentsApiUrl", "/api/petitions/" + petition.getId() + "/comments");
            modelAndView.addObject("attachmentApiUrl", "/api/petitions/" + petition.getId() + "/attachments");
            modelAndView.addObject("linkedPetitionsApiUrl", "/api/petitions/" + petition.getId() + "/linked");
            modelAndView.addObject("linkedPetitionerApiUrl", "/api/petitions/" + petition.getId() + "/by/petitioner");
            modelAndView.addObject("emailsApiUrl", "/api/petitions/" + petition.getId() + "/emails");
        }
        addCustomParams(modelAndView);
        return modelAndView;
    }

    @RequestMapping(path = "/petition", method = RequestMethod.GET)
    public ModelAndView addPetition() {
        Petitioner petitioner = new Petitioner();
        petitioner.setCountry(defaultsConfig.getCountry());

        Petition petition = new Petition();
        petition.setReceivedDate(new Date());
        petition.setDeadline(DateUtil.deadline(new Date(), deadlineConfig.getDays()));
        petition.setPetitioner(petitioner);

        petitionCustomParamService.initDefaults(petition);

        return editPetition(petition);
    }

    @RequestMapping(path = "/petition/{id}", method = RequestMethod.GET)
    public ModelAndView editPetition(@PathVariable("id") Long id, HttpServletRequest request) {
        String url = StringUtil.toRelativeURL(request.getHeader("referer"), "petitions)");

        Petition petition = petitionService.findById(id);
        check(assertNotNull(petition, i18n("controller.petition.invalid_id")), logger, redirect(url));

        return editPetition(petition);
    }

    @RequestMapping(path = "/petition/fromEmail/{id}", method = RequestMethod.GET)
    public ModelAndView createPetitionFromEmail(@PathVariable("id") Long id, HttpServletRequest request) {
        String url = StringUtil.toRelativeURL(request.getHeader("referer"), "petitions)");

        Email email = emailService.searchById(id);
        check(assertNull(email.getPetition(), i18n("controller.petition.petition_exists_for_email")), logger, redirect(url));

        Petition petition = petitionService.createFromEmail(email);
        check(assertNotNull(petition, i18n("controller.petition.invalid_email")), logger, redirect(url));

        petitionCustomParamService.initDefaults(petition);
        return editPetition(petition);
    }

    @RequestMapping(path = "/petition", method = RequestMethod.POST)
    public ModelAndView savePetition(@Valid Petition petition, BindingResult bindingResult, final RedirectAttributes attr) {
        petitionCustomParamService.validate(petition, bindingResult);

        check(bindingResult, i18n("controller.petition.petition_not_saved"), logger, editPetition(petition));
        petition = petitionService.save(petition);
        return successAndRedirect(i18n("controller.petition.petition_saved"), "petition/" + petition.getId(), attr);
    }

    @RequestMapping(value = "/petition/redirect/{id}", method = RequestMethod.GET)
    public ModelAndView redirectPetition(@PathVariable("id") long id, HttpServletRequest request) {
        String url = StringUtil.toRelativeURL(request.getHeader("referer"), "petitions)");

        Petition petition = petitionService.findById(id);
        check(assertNotNull(petition, i18n("controller.petition.invalid_id")), logger, redirect(url));

        ModelAndView modelAndView = new ModelAndView("petitions_redirect");
        modelAndView.addObject("petition", petition);
        modelAndView.addObject("contacts", contactService.getAllContacts());
        modelAndView.addObject("templateList", emailTemplateService.findByCategory(EmailTemplate.Category.forward));
        return modelAndView;
    }

    @RequestMapping(value = "/petition/redirect/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView redirectPetition(@PathVariable("id") long id,
                                         @RequestParam("subject") String subject,
                                         @RequestParam("recipients") long[] recipients,
                                         @RequestParam(value = "attachments[]", required = false) Long[] attachments,
                                         @RequestParam("description") String description,
                                         final RedirectAttributes attr, HttpServletRequest request) {
        String url = StringUtil.toRelativeURL(request.getHeader("referer"), "petitions)");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        check(assertNotNull(user, i18n("controller.petition.invalid_user")), logger, redirect(url));
        Petition petition = petitionService.findById(id);
        check(assertNotNull(petition, i18n("controller.petition.invalid_id")), logger, redirect(url));

        statusService.create(PetitionStatus.Status.REDIRECTED, petition, user);

        try {
            smtpService.send(createEmail(subject, description, convertRecipients(recipients), attachments, petition));
        } catch (Exception e) {
            check(fail(i18n("controller.petition.petition_not_redirected")), logger, redirect("petition/" + petition.getId()));
        	logger.error(i18n("controller.petition.petition_not_redirected"), e);
        }

        return successAndRedirect(i18n("controller.petition.petition_redirected"), "petition/" + petition.getId(), attr);
    }

    @RequestMapping(value = "/petition/{pid}/resolve/{action}", method = RequestMethod.GET)
    public ModelAndView resolve(@PathVariable("pid") Long pid, @PathVariable("action") String action, HttpServletRequest request) {
        String url = StringUtil.toRelativeURL(request.getHeader("referer"), "petitions)");
        Petition petition = petitionService.findById(pid);
        check(assertNotNull(petition, i18n("controller.petition.invalid_id")), logger, redirect(url));

        check(assertEquals(petition.getCurrentStatus(), PetitionStatus.Status.IN_PROGRESS, i18n("controller.petition.resolve_work_not_started")), logger, redirect(url));

        ModelAndView modelAndView = new ModelAndView("petitions_resolve");
        modelAndView.addObject("action", action);
        modelAndView.addObject("pid", pid);
        modelAndView.addObject("pEmail", petition.getPetitioner().getEmail());
        modelAndView.addObject("attachmentApiUrl", "/api/petitions/" + pid + "/attachments");
        modelAndView.addObject("linkedPetitionsApiUrl", "/api/petitions/" + pid + "/linked");
        modelAndView.addObject("linkedPetitionerApiUrl", "/api/petitions/" + pid + "/by/petitioner");
        modelAndView.addObject("templateList", emailTemplateService.findByCategory(EmailTemplate.Category.response));
        return modelAndView;
    }

    @RequestMapping(value = "/petition/{pid}/resolve/{action}", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView resolvePetition(@PathVariable("pid") long id,
                                        @PathVariable("action") String action,
                                        @RequestParam("resolution") PetitionStatus.Resolution resolution,
                                        @RequestParam("email") boolean sendEmail,
                                        @RequestParam(value = "attachments[]", required = false) Long[] attachments,
                                        @RequestParam("description") String description,
                                        final RedirectAttributes attr, HttpServletRequest request) {
        String url = StringUtil.toRelativeURL(request.getHeader("referer"), "petitions)");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        check(assertNotNull(user, i18n("controller.petition.invalid_user")), logger, redirect(url));
        Petition petition = petitionService.findById(id);
        check(assertNotNull(petition, i18n("controller.petition.invalid_id")), logger, redirect(url));

        check(validateSolutionParameters(petition, resolution, sendEmail, description), logger, redirect("petition/" + id + "/resolve/" + action));

        PetitionStatus.Status status = resolution.getStatus();
        statusService.create(status, petition, user);

        if (sendEmail) {
            try {
                String subject = i18n("controller.petition.resolve_resolution") + ": " + i18n(resolution);
                smtpService.send(createEmail(subject, description, petition.getPetitioner().getEmail(), attachments, petition));
            } catch (Exception e) {
                logger.error(i18n("controller.petition.resolve_failed"), e);
                check(fail(i18n("controller.petition.resolve_failed")), logger, redirect("petition/" + petition.getId()));
            }
        } else {
            String message = i18n("controller.petition.resolve_resolution") + ": " + i18n(resolution) + " \n <br/> " + description;
            check(failOnException(() -> commentService.createAndSave(user, petition, message), i18n("controller.petition.resolve_failed")),
                  logger, redirect("petition/" + petition.getId()));
        }

        return successAndRedirect(i18n("controller.petition.resolve_successful"), "petition/" + petition.getId(), attr);
    }

    private void addCustomParams(ModelAndView modelAndView) {
        modelAndView.addObject("user_list", userService.getAllUsers());

        for (PetitionCustomParam.Type type : PetitionCustomParam.Type.values()) {
        	PetitionCustomParam param = petitionCustomParamService.findByType(type);
        	if (param == null) {
        	    logger.error("Custom param missing for type = " + type);
            } else {
                modelAndView.addObject(type.name(), param);
            }
        }
    }

    private ValidationStatus validateSolutionParameters(Petition petition, PetitionStatus.Resolution resolution,
                                                        boolean sendEmail, String description) {
        if (petition.getCurrentStatus() != PetitionStatus.Status.IN_PROGRESS) {
        	return new ValidationStatus("controller.petition.resolve_work_not_started");
        }

        if (sendEmail && (description == null || description.trim().isEmpty())) {
        	return new ValidationStatus("controller.petition.resolve_message_required");
        }

        if (Objects.equals(resolution, PetitionStatus.Resolution.duplicate) && petitionService.countLinkedPetitions(petition) == 0) {
        	return new ValidationStatus("controller.petition.resolve_duplicate_petition");
        }

        return new ValidationStatus();
    }

    private Email createEmail(String subject, String description, String recipients, Long[] attachments, Petition petition) {
        List<Attachment> attachmentList = attachmentService.findByIds(attachments);

        Email email = new Email();
        email.setBody(description);
        email.setDate(new Date());
        email.setSubject(subject);
        email.setSender(smtpConfig.getUsername());
        email.setRecipients(recipients);
        email.setAttachments(attachmentList);
        email.setPetition(petition);
        email.setType(Email.EmailType.Outbox);
    	return emailService.save(email);
    }

    private String convertRecipients(long[] recipients) {
        StringBuilder recipientString = new StringBuilder("");
        for (long rid : recipients) {
            Contact contact = contactService.getById(rid);
            recipientString.append(contact.getName()).append(" <").append(contact.getEmail()).append(">, ");
        }
        return recipientString.toString();
    }
}