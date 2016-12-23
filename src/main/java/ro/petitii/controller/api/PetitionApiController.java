package ro.petitii.controller.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
import ro.petitii.model.*;
import ro.petitii.model.datatables.AttachmentResponse;
import ro.petitii.model.datatables.CommentResponse;
import ro.petitii.model.datatables.PetitionResponse;
import ro.petitii.service.*;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static ro.petitii.util.CleanUtil.cleanHtml;

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

    // will answer to compounded URL /api/petitions/user
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getUserPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);

        PetitionStatus.Status pStatus = parseStatus(status);

        Integer start = input.getStart();
        Integer length = input.getLength();
        DataTablesOutput<PetitionResponse> response = petitionService
                .getTableContent(user, pStatus, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);

        return response;
    }

    @RequestMapping(value = "/all", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<PetitionResponse> getAllPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();

        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        PetitionStatus.Status pStatus = parseStatus(status);
        Integer start = input.getStart();
        Integer length = input.getLength();
        DataTablesOutput<PetitionResponse> response = petitionService
                .getTableContent(null, pStatus, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachments", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<AttachmentResponse> getAllAttachments(@PathVariable("id") Long id,
                                                                  @Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();

        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Integer start = input.getStart();
        Integer length = input.getLength();
        DataTablesOutput<AttachmentResponse> response = attachmentService
                .getTableContent(petition, start, length, sortDirection, sortColumn);
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

    private PetitionStatus.Status parseStatus(String status) {
        if ("started".equalsIgnoreCase(status)) {
            return PetitionStatus.Status.IN_PROGRESS;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/{id}/comments", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<CommentResponse> getAllComments(@PathVariable("id") Long id, @Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();

        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Integer start = input.getStart();
        Integer length = input.getLength();
        DataTablesOutput<CommentResponse> response = commentService
                .getTableContent(petition, start, length, sortDirection, sortColumn);
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

        Comment comment = new Comment();
        comment.setComment(cleanHtml(commentBody));
        comment.setUser(user);
        comment.setDate(new Date());
        comment.setPetition(petition);

        commentService.save(comment);

        return "done";
    }

    @RequestMapping(value = "/{pid}/comment/{cid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteComment(@PathVariable("cid") Long cid) {
        commentService.delete(cid);
        return "done";
    }

    @RequestMapping(value = "/start-work", method = RequestMethod.POST)
    @ResponseBody
    public  Map<String, String> startWork(@RequestParam("petitions[]") long[] petitionIds) {
        Map<String, String> result = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        List<Long> errors = new ArrayList<>();

        for (long id : petitionIds) {
            Petition pet = petitionService.findById(id);
            if ((pet!=null)&&(pet.getCurrentStatus().equals(PetitionStatus.Status.RECEIVED))) {
                statusService.create(PetitionStatus.Status.IN_PROGRESS,pet,user);
            } else {
                errors.add(id);
            }
        }

        if (errors.size()>0)
            if (errors.size()>0) {
                result.put("success","false");
                result.put("errorMsg", "Statusul nu a fost modificat pentru petitiile: " + errors.toString());
            }
        else {
            result.put("success","true");
            result.put("errorMsg","Statusul petitiilor a fost modificat.");
        }

        return result;
    }
}
