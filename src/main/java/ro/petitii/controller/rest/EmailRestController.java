package ro.petitii.controller.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ro.petitii.model.Attachment;
import ro.petitii.model.Email;
import ro.petitii.model.rest.RestEmailResponse;
import ro.petitii.service.EmailService;
import ro.petitii.service.email.ImapService;
import ro.petitii.util.Pair;
import ro.petitii.util.ZipUtils;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
public class EmailRestController {
    @Autowired
    EmailService emailService;

    @Autowired
    ImapService imapService;


    private static final Logger LOGGER = LoggerFactory.getLogger(EmailRestController.class);

    @RequestMapping(value = "/rest/emails", method = RequestMethod.POST)
    @ResponseBody
    public RestEmailResponse getInbox(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) sortDirection = Sort.Direction.ASC;
        else if (input.getOrder().get(0).getDir().equals("desc")) sortDirection = Sort.Direction.DESC;
        RestEmailResponse response = emailService.getTableContent(Email.EmailType.Inbox, input.getStart(), input.getLength(), sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/rest/spam", method = RequestMethod.POST)
    @ResponseBody
    public RestEmailResponse getSpam(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) sortDirection = Sort.Direction.ASC;
        else if (input.getOrder().get(0).getDir().equals("desc")) sortDirection = Sort.Direction.DESC;
        RestEmailResponse response = emailService.getTableContent(Email.EmailType.Spam, input.getStart(), input.getLength(), sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/rest/markAs", method = RequestMethod.GET)
    @ResponseBody
    public String markSpam(@RequestParam("type") String type, @RequestParam("id") Long id) {
        Email.EmailType emailType = null;
        if ("email".equalsIgnoreCase(type)) {
            emailType = Email.EmailType.Inbox;
        } else if ("spam".equalsIgnoreCase(type)) {
            emailType = Email.EmailType.Spam;
        }

        if (emailType == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Email email = emailService.searchById(id);
        if (email == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        email.setType(emailType);
        emailService.saveAlone(email);
        return "OK";
    }

    @RequestMapping("/rest/refresh")
    @ResponseBody
    public Map<String, String> inboxRefresh() {
        Map<String, String> result = new HashMap<String, String>();
        try {
            imapService.getMail();
        } catch (Exception e) {
            result.put("error", e.getClass().getName());
            result.put("errorMsg", e.getMessage());
        }
        if (!result.containsKey("error")) {
            result.put("success", "true");
        } else {
            result.put("success", "false");
        }
        return result;
    }

    @RequestMapping("/rest/email/{id}/attachments/zip")
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
}
