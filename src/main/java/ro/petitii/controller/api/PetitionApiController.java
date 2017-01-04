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
import ro.petitii.model.datatables.PetitionResponse;
import ro.petitii.service.*;
import ro.petitii.service.email.SmtpService;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static ro.petitii.controller.api.DatatableUtils.pageRequest;
import static ro.petitii.util.StringUtil.cleanHtml;

@Controller
@ControllerAdvice
// url base for all class methods
@RequestMapping("/api/petitions")
public class PetitionApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetitionApiController.class);

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

        DataTablesOutput<PetitionResponse> response = petitionService.getTableContent(user, pStatus, pageRequest(input, PetitionResponse.sortMapping));
        response.setDraw(sequenceNo);

        return response;
    }

    @RequestMapping(value = "/all", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getAllPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();
        List<PetitionStatus.Status> pStatus = parseStatus(status);
        DataTablesOutput<PetitionResponse> response = petitionService.getTableContent(null, pStatus, pageRequest(input, PetitionResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "{id}/by/petitioner", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getPetitionsByPetitioner(@Valid DataTablesInput input,
                                                                       @PathVariable("id") long id) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        DataTablesOutput<PetitionResponse> response = petitionService
                .getTableContent(petition, petition.getPetitioner(), pageRequest(input, PetitionResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "{id}/linked", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getLinkedPetitions(@Valid DataTablesInput input,
                                                                 @PathVariable("id") long id) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        DataTablesOutput<PetitionResponse> response = petitionService
                .getTableLinkedPetitions(petition, pageRequest(input, PetitionResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachments", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<AttachmentResponse> getAllAttachments(@PathVariable("id") Long id,
                                                                  @Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        DataTablesOutput<AttachmentResponse> response = attachmentService
                .getTableContent(petition, pageRequest(input, AttachmentResponse.sortMapping));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachment/add", method = RequestMethod.POST)
    @ResponseBody
    public void addAttachment(@RequestParam("files") MultipartFile[] files,
                              @PathVariable("id") Long petitionId) throws IOException {
        attachmentService.saveFromForm(files, petitionId);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    protected ResponseEntity<String> handleMaxUploadSizeExceededException(HttpServletRequest request,
                                                                          HttpServletResponse response,
                                                                          Throwable e) throws IOException {
        LOGGER.warn(e.getMessage());
        return ResponseEntity.unprocessableEntity().body("Fișierul depășește mărimea admisă de server");
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    protected ResponseEntity<String> handleGenericMultipartException(final HttpServletRequest request,
                                                                     final HttpServletResponse response,
                                                                     final Throwable e) throws IOException {
        Throwable rootCause = e;
        Throwable cause = e.getCause();
        while (cause != null && !cause.equals(rootCause)) {
            rootCause = cause;
            cause = cause.getCause();
        }
        LOGGER.error(rootCause.getMessage());
        return ResponseEntity.unprocessableEntity().body("Fișierul depășește mărimea admisă de server");
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
            LOGGER.error("Could not find attachment with id " + id + " on disk: " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find attachment with id " + id + ": " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{pid}/attachment/{aid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteAttachment(@PathVariable("aid") Long id) {
        attachmentService.deleteFromPetition(id);
        return "done";
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
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        DataTablesOutput<CommentResponse> response = commentService.getTableContent(petition, pageRequest(input));
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/comment/add", method = RequestMethod.POST)
    @ResponseBody
    public String addComment(@PathVariable("id") Long id, @RequestBody String commentBody) {
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<User> users = userService.findUserByEmail(auth.getName());
        if (users.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        User user = users.get(0);

        commentService.createAndSave(user, petition, cleanHtml(commentBody));
        return "done";
    }

    @RequestMapping(value = "/{pid}/comment/{cid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteComment(@PathVariable("cid") Long cid) {
        commentService.delete(cid);
        return "done";
    }

    @RequestMapping(value = "/{pid}/link/{vid}", method = RequestMethod.POST)
    @ResponseBody
    public String linkPetitions(@PathVariable("pid") Long pid, @PathVariable("vid") Long vid) {
        Petition petition = petitionService.findById(pid);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        Petition vassal = petitionService.findById(vid);
        if (vassal == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        connectionService.link(petition, vassal);
        return "done";
    }

    @RequestMapping(value = "/{pid}/unlink/{vid}", method = RequestMethod.POST)
    @ResponseBody
    public String unlinkPetitions(@PathVariable("pid") Long pid, @PathVariable("vid") Long vid) {
        Petition petition = petitionService.findById(pid);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        Petition vassal = petitionService.findById(vid);
        if (vassal == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        connectionService.unlink(petition, vassal);
        return "done";
    }

    @RequestMapping(value = "/start-work", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> startWork(@RequestParam("petitions[]") long[] petitionIds) {
        Map<String, String> result = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        List<String> errors = new LinkedList<>();

        for (long id : petitionIds) {
            Petition pet = petitionService.findById(id);
            if (pet == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }
            if (pet.getCurrentStatus().equals(PetitionStatus.Status.RECEIVED)) {
                statusService.create(PetitionStatus.Status.IN_PROGRESS, pet, user);
                Email email = new Email();
                email.setSender(config.getUsername());
                email.setSubject("Petitia dvs. a fost inregistrata");
                email.setRecipients(pet.getPetitioner().getEmail());
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                // todo: insert actual template
                email.setBody("Petitia dvs a fost inregistrata cu numarul " + pet.getRegNo().toString() + " pe data de " + df.format(pet.getReceivedDate()) + ". Termen de solutionare: " + df.format(pet.getDeadline()));
                try {
                    smtpService.send(email);
                } catch (MessagingException e) {
                    LOGGER.error("Could not send email with registration number " + pet.getRegNo().toString());
                }
            } else {
                errors.add(pet.getRegNo().getNumber());
            }
        }

        if (errors.size() > 0) {
            result.put("success", "false");
            String errorList = errors.stream().collect(Collectors.joining(", "));
            result.put("errorMsg", "Statusul nu a fost modificat pentru petițiile: " + errorList);
        } else {
            result.put("success", "true");
            result.put("errorMsg", "Statusul petițiilor a fost modificat.");
        }

        return result;
    }
}
