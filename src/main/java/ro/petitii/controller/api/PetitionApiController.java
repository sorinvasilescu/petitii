package ro.petitii.controller.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import ro.petitii.config.SmtpConfig;
import ro.petitii.model.*;
import ro.petitii.model.datatables.AttachmentResponse;
import ro.petitii.model.datatables.CommentResponse;
import ro.petitii.model.datatables.EmailResponse;
import ro.petitii.model.datatables.PetitionResponse;
import ro.petitii.service.*;
import ro.petitii.service.email.SmtpService;
import ro.petitii.service.template.EmailTemplateProcessorService;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ro.petitii.controller.api.DatatableUtils.pageRequest;
import static ro.petitii.util.StringUtil.cleanHtml;

@Controller
// url base for all class methods
@RequestMapping("/api/petitions")
public class PetitionApiController extends ApiController {
    private static final Logger logger = LoggerFactory.getLogger(PetitionApiController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PetitionService petitionService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PetitionStatusService statusService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private EmailService emailService;


    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private EmailTemplateProcessorService emailTemplateProcessorService;

    @Autowired
    private SmtpConfig config;

    @Autowired
    private SmtpService smtpService;

	// will answer to compounded URL /api/petitions/user
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getUserPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);

        List<PetitionStatus.Status> pStatus = parseStatus(status);

        DataTablesOutput<PetitionResponse> response = petitionService.getTableContent(input, user, pStatus);
        response.setDraw(sequenceNo);

        return response;
    }

    @RequestMapping(value = "/all", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getAllPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();
        List<PetitionStatus.Status> pStatus = parseStatus(status);
        DataTablesOutput<PetitionResponse> response = petitionService.getTableContent(input, null, pStatus);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "{id}/by/petitioner", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getPetitionsByPetitioner(@Valid DataTablesInput input, @PathVariable("id") long id) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            return new DataTablesOutput<>();
        }

        DataTablesOutput<PetitionResponse> response = petitionService.getTableContent(petition, petition.getPetitioner(),
                                                                                      pageRequest(input, PetitionResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "{id}/linked", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getLinkedPetitions(@Valid DataTablesInput input, @PathVariable("id") long id) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            return new DataTablesOutput<>();
        }
        DataTablesOutput<PetitionResponse> response = petitionService.getTableLinkedPetitions(petition, pageRequest(input, PetitionResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "{id}/emails", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<EmailResponse> getEmails(@Valid DataTablesInput input, @PathVariable("id") long id) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            return new DataTablesOutput<>();
        }
        DataTablesOutput<EmailResponse> response = emailService.getTableContent(input, petition);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachments", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<AttachmentResponse> getAllAttachments(@PathVariable("id") Long id, @Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            return new DataTablesOutput<>();
        }

        DataTablesOutput<AttachmentResponse> response = attachmentService.getTableContent(petition, pageRequest(input, AttachmentResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachment/add", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult addAttachment(@RequestParam("files") MultipartFile[] files, @PathVariable("id") Long petitionId) throws IOException {
    	try {
            attachmentService.saveFromForm(files, petitionId);
            return success();
        } catch (Exception e) {
    	    logger.error("Could not save attachment for petition = " + petitionId, e);
    	    return fail("api.controller.petition.attachment.adding.failed");
        }
    }

    @RequestMapping(value = "/{pid}/attachment/{aid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult deleteAttachment(@PathVariable("aid") Long id) {
        try {
            attachmentService.deleteFromPetition(id);
            return success();
        } catch (Exception e) {
            logger.error("Could not delete attachment = " + id, e);
            return fail("api.controller.petition.attachment.delete.failed");
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    protected ResponseEntity<String> handleMaxUploadSizeExceededException(Throwable e) throws IOException {
        logger.warn("Max upload size exceeded", e);
        String message = i18n("api.controller.petition.attachment_size_exceeded");
        return ResponseEntity.unprocessableEntity().body(message);
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    protected ResponseEntity<String> handleGenericMultipartException(final Throwable e) throws IOException {
        Throwable rootCause = e;
        Throwable cause = e.getCause();
        while (cause != null && !cause.equals(rootCause)) {
            rootCause = cause;
            cause = cause.getCause();
        }
        logger.error(rootCause.getMessage(), e);
        String message = i18n("api.controller.petition.attachment_size_exceeded");
        return ResponseEntity.unprocessableEntity().body(message);
    }

    @RequestMapping("/{id}/attachments/zip")
    public void downloadAllFromPetition(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Petition petition = petitionService.findById(id);
            if (petition == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }

            List<Pair<String, Path>> attachments = new LinkedList<>();
            for (Attachment att : petition.getAttachments()) {
                attachments.add(new Pair<>(att.getOriginalFilename(), Paths.get(att.getFilename())));
            }
            String zipFilename = "petitie-" + id + ".zip";
            InputStream is = ZipUtils.create(attachments);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + zipFilename);
            IOUtils.copy(is, response.getOutputStream());
            is.close();
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("Could not find attachment with id " + id + " on disk", e);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find attachment with id " + id, e);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    private List<PetitionStatus.Status> parseStatus(String status) {
        if ("started".equalsIgnoreCase(status)) {
            List<PetitionStatus.Status> statuses = new LinkedList<>();
            statuses.add(PetitionStatus.Status.RECEIVED);
            statuses.add(PetitionStatus.Status.IN_PROGRESS);
            return statuses;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/{id}/comments", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<CommentResponse> getAllComments(@PathVariable("id") Long id, @Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            return new DataTablesOutput<>();
        }
        DataTablesOutput<CommentResponse> response = commentService.getTableContent(petition, pageRequest(input));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/comment/add", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult addComment(@PathVariable("id") Long id, @RequestBody String commentBody) {
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            return fail("api.controller.petition.invalid_petition_id");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<User> users = userService.findUserByEmail(auth.getName());
        if (users.isEmpty()) {
            return fail("api.controller.petition.invalid_user");
        }
        User user = users.get(0);

        try {
            commentService.createAndSave(user, petition, cleanHtml(commentBody));
            return success();
        } catch (Exception e) {
            logger.error("Could not add comment to petition = " + id, e);
            return fail("api.controller.petition.comment.add.failed");
        }
    }

    @RequestMapping(value = "/{pid}/comment/{cid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult deleteComment(@PathVariable("cid") Long cid) {
    	try {
            commentService.delete(cid);
            return success();
        } catch (Exception e) {
    	    logger.error("Could not delete comment = " + cid, e);
    	    return fail("api.controller.petition.comment.delete.failed");
        }
    }

    @RequestMapping(value = "/{pid}/link/{vid}", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult linkPetitions(@PathVariable("pid") Long pid, @PathVariable("vid") Long vid) {
        Petition petition = petitionService.findById(pid);
        if (petition == null) {
            return fail("api.controller.petition.invalid_petition_id");
        }
        Petition vassal = petitionService.findById(vid);
        if (vassal == null) {
            return fail("api.controller.petition.invalid_petition_id");
        }

        try {
            connectionService.link(petition, vassal);
            return success();
        } catch (Exception e) {
            logger.error("Cannot link petition = " + petition + " -> " + vid, e);
            return fail("api.controller.petition.link.add.failed");
        }
    }

    @RequestMapping(value = "/{pid}/unlink/{vid}", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult unlinkPetitions(@PathVariable("pid") Long pid, @PathVariable("vid") Long vid) {
        Petition petition = petitionService.findById(pid);
        if (petition == null) {
            return fail("api.controller.petition.invalid_petition_id");
        }
        Petition vassal = petitionService.findById(vid);
        if (vassal == null) {
            return fail("api.controller.petition.invalid_petition_id");
        }

        try {
            connectionService.unlink(petition, vassal);
            return success();
        } catch (Exception e) {
            logger.error("Cannot unlink petition = " + petition + " -> " + vid, e);
            return fail("api.controller.petition.link.delete.failed");
        }
    }

    @RequestMapping(value = "/start-work", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult startWork(@RequestParam("petitions[]") long[] petitionIds) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        if (user == null) {
            return fail("api.controller.petition.invalid_user");
        }
        List<String> errors = new LinkedList<>();
        String warning = null;

        EmailTemplate emailTemplate = emailTemplateService.findOneByCategory(EmailTemplate.Category.start_work);
        if (emailTemplate == null) {
            logger.error("Email template not found for start work, using standard text messages ...");
            warning = i18n("api.controller.petition.email_template_config_error");
        }

        for (long id : petitionIds) {
            Petition pet = petitionService.findById(id);
            if (pet == null) {
                errors.add("" + id);
            } else {
                if (pet.getCurrentStatus().equals(PetitionStatus.Status.RECEIVED)) {
                    statusService.create(PetitionStatus.Status.IN_PROGRESS, pet, user);
                    Email email = new Email();
                    email.setSender(config.getUsername());
                    email.setSubject(i18n("api.controller.petition.registered_email_subject"));
                    email.setRecipients(pet.getPetitioner().getEmail());

                    try {
                        if (emailTemplate == null) {
                            email.setBody(createDefaultEmail(pet));
                        } else {
                            Map<String, Object> vars = new HashMap<>();
                            vars.put("pet", pet);
                            vars.put("petition", pet);
                            String body = emailTemplateProcessorService
                                    .processTemplateWithId(emailTemplate.getId(), vars);
                            if (body == null) {
                                body = createDefaultEmail(pet);
                            }
                            logger.info("Sending start-work email: " + body);
                            email.setBody(body);
                        }
                        smtpService.send(email);
                    } catch (MessagingException e) {
                        logger.error("Could not send email with registration number " + pet.getRegNo().toString(), e);
                    }
                } else {
                    errors.add(pet.getRegNo().getNumber());
                }
            }
        }

        if (errors.size() > 0) {
            String errorList = errors.stream().collect(Collectors.joining(", "));
            return fail("api.controller.petition.status_not_updated", errorList, warning);
        } else {
            return success("api.controller.petition.status_updated");
        }
    }

    private String createDefaultEmail(Petition petition) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String recDate = df.format(petition.getReceivedDate());
        String deadline = df.format(petition.getDeadline());

        String[] params = new String[]{petition.getRegNo().getNumber(), recDate, deadline};
        return i18n("api.controller.petition.registered", params);
    }
}
