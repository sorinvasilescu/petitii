package ro.petitii.controller.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import ro.petitii.model.*;
import ro.petitii.model.dt.MiniComment;
import ro.petitii.model.dt.RestAttachmentResponse;
import ro.petitii.model.dt.RestCommentResponse;
import ro.petitii.model.dt.RestPetitionResponse;
import ro.petitii.service.AttachmentService;
import ro.petitii.service.CommentService;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Controller
// url base for all class methods
@RequestMapping("/rest/petitions")
public class PetitionRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PetitionRestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PetitionService petitionService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private CommentService commentService;

    // will answer to compounded URL /rest/petitions/user
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public RestPetitionResponse getUserPetitions(@Valid DataTablesInput input, String status) {
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
        RestPetitionResponse response = petitionService.getTableContent(user, pStatus, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);

        return response;
    }

    @RequestMapping(value = "/all", method = RequestMethod.POST)
    @ResponseBody
    public RestPetitionResponse getAllPetitions(@Valid DataTablesInput input, String status) {
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
        RestPetitionResponse response =
                petitionService.getTableContent(null, pStatus, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachments", method = RequestMethod.POST)
    @ResponseBody
    public RestAttachmentResponse getAllAttachments(@PathVariable("id") Long id, @Valid DataTablesInput input) {
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
        RestAttachmentResponse response = attachmentService.getTableContent(petition, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/attachments/add", method = RequestMethod.POST)
    @ResponseBody
    public void addAttachment(@RequestParam("files") MultipartFile[] files, @PathVariable("id") Long petitionId) throws IOException {
        attachmentService.saveFromForm(files,petitionId);
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

    @RequestMapping(value = "/{pid}/attachments/{aid}/delete", method = RequestMethod.POST)
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
    public RestCommentResponse getAllComments(@PathVariable("id") Long id, @Valid DataTablesInput input) {
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
        RestCommentResponse response = commentService.getTableContent(petition, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/{id}/comments/add", method = RequestMethod.POST, consumes={"application/json"})
    @ResponseBody
    public String addComment(@PathVariable("id") Long id, @RequestBody MiniComment miniComment) {
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
        comment.setComment(miniComment.getComment());
        comment.setUser(user);
        comment.setDate(new Date());
        comment.setPetition(petition);

        commentService.save(comment);

        return "done";
    }

    @RequestMapping(value = "/{pid}/comments/{cid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteComment(@PathVariable("cid") Long cid) {
        commentService.delete(cid);
        return "done";
    }
}
