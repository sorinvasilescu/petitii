package ro.petitii.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ro.petitii.model.Email;
import ro.petitii.model.rest.RestEmailResponse;
import ro.petitii.service.EmailService;
import ro.petitii.service.email.ImapService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EmailRestController {
    @Autowired
    EmailService emailService;

    @Autowired
    ImapService imapService;

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
        emailService.save(email);
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
}
