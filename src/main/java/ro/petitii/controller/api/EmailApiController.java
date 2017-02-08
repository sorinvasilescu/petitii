package ro.petitii.controller.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ro.petitii.model.Attachment;
import ro.petitii.model.Email;
import ro.petitii.model.datatables.EmailResponse;
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
import java.util.LinkedList;
import java.util.List;

@RestController
public class EmailApiController extends ApiController {
    private static final Logger logger = LoggerFactory.getLogger(EmailApiController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private ImapService imapService;

    @RequestMapping(value = "/api/emails", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<EmailResponse> getInbox(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        DataTablesOutput<EmailResponse> response = emailService.getTableContent(input, Email.EmailType.Inbox);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/api/spam", method = RequestMethod.POST)
    @ResponseBody
    public DataTablesOutput<EmailResponse> getSpam(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        DataTablesOutput<EmailResponse> response = emailService.getTableContent(input, Email.EmailType.Spam);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/api/markAs/{type}/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ApiResult markSpam(@PathVariable("type") String type, @PathVariable("id") Long id) {
        Email.EmailType emailType = null;
        if ("email".equalsIgnoreCase(type)) {
            emailType = Email.EmailType.Inbox;
        } else if ("spam".equalsIgnoreCase(type)) {
            emailType = Email.EmailType.Spam;
        }

        if (emailType == null) {
            return fail("api.controller.email.invalid_type");
        }

        Email email = emailService.searchById(id);
        if (email == null) {
            return fail("api.controller.email.invalid_id");
        }
        email.setType(emailType);
        try {
            emailService.save(email);
            return success("api.controller.email.markSpam.success");
        } catch (Exception e) {
            logger.error("Could set email = " + id + "as " + emailType, e);
            return fail("api.controller.email.markSpam.failed");
        }
    }

    @RequestMapping("/api/refresh")
    @ResponseBody
    public ApiResult inboxRefresh() {
        try {
            imapService.getMail();
            return success("api.controller.email.refresh.success");
        } catch (Exception e) {
            logger.error("Cannot read from inbox:", e);
            return fail("api.controller.email.refresh.failed", e.getMessage());
        }
    }

    @RequestMapping("/api/email/{id}/attachments/zip")
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
            logger.error("Could not find attachment with id " + id + " on disk", e);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find attachment with id " + id, e);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Could not download attachment with id " + id, e);
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
