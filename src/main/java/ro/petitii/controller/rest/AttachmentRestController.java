package ro.petitii.controller.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ro.petitii.controller.EmailController;
import ro.petitii.model.*;
import ro.petitii.model.rest.MiniComment;
import ro.petitii.model.rest.RestAttachmentResponse;
import ro.petitii.model.rest.RestCommentResponse;
import ro.petitii.service.*;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Controller
public class AttachmentRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentRestController.class);

    private PetitionService petitionService;
    private UserService userService;
    private EmailService emailService;
    private AttachmentService attachmentService;

    @Inject
    public AttachmentRestController(PetitionService petitionService, UserService userService,
                                    EmailService emailService, AttachmentService attachmentService) {
        this.petitionService = petitionService;
        this.userService = userService;
        this.emailService = emailService;
        this.attachmentService = attachmentService;
    }

    @RequestMapping(value = "/rest/attachments/{id}", method = RequestMethod.POST)
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
        RestAttachmentResponse response = attachmentService
                .getTableContent(petition, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

//    @RequestMapping(value = "/rest/petition/{pid}/attachment/add", method = RequestMethod.POST, consumes = {"application/json"})
//    @ResponseBody
//    public String addComment(@PathVariable("pid") Long id, @RequestBody MiniComment miniComment) {
//        Petition petition = petitionService.findById(id);
//        if (petition == null) {
//            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
//        }
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        List<User> users = userService.findUserByEmail(auth.getName());
//        if (users.isEmpty()) {
//            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
//        }
//        User user = users.get(0);
//
//        Comment comment = new Comment();
//        comment.setComment(miniComment.getComment());
//        comment.setUser(user);
//        comment.setDate(new Date());
//        comment.setPetition(petition);
//
//        commentService.save(comment);
//
//        return "done";
//    }

    @RequestMapping(value = "/rest/petition/attachment/delete/{cid}", method = RequestMethod.POST)
    @ResponseBody
    public String deleteComment(@PathVariable("cid") Long cid) {
        attachmentService.delete(cid);
        return "done";
    }

    @RequestMapping("/action/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Attachment att = attachmentService.findById(id);
            Path filepath = Paths.get(att.getFilename());
            FileInputStream is = new FileInputStream(new File(filepath.toUri()));
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + att.getOriginalFilename());
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

    @RequestMapping("/action/download/email/{id}")
    public void downloadAllFromEmail(@PathVariable("id") Long id, HttpServletResponse response) {
        try {

            Email email = emailService.searchById(id);
            if (email == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }

            List<Pair<String, Path>> attachments = new LinkedList<>();
            for (Attachment att : email.getAttachments()) {
                attachments.add(new Pair<>(att.getOriginalFilename(), Paths.get(att.getFilename())));
            }
            String zipFilename = "email-" + id + ".zip";
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

    @RequestMapping("/action/download/petition/{id}")
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
            String zipFilename = "email-" + id + ".zip";
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
}
