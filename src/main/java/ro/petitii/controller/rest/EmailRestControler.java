package ro.petitii.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.web.bind.annotation.*;
import ro.petitii.model.Email;
import ro.petitii.model.rest.RestEmailResponse;
import ro.petitii.service.EmailService;
import ro.petitii.service.email.ImapService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.validation.Valid;

@RestController
public class EmailRestControler {

    @Autowired
    EmailService emailService;
    
    @Autowired
    ImapService imapService;

    @RequestMapping(value = "/rest/emails", method = RequestMethod.POST)
    @ResponseBody
    RestEmailResponse getInbox(@Valid DataTablesInput input) {
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
    RestEmailResponse getSpam(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) sortDirection = Sort.Direction.ASC;
        else if (input.getOrder().get(0).getDir().equals("desc")) sortDirection = Sort.Direction.DESC;
        RestEmailResponse response = emailService.getTableContent(Email.EmailType.Spam, input.getStart(), input.getLength(), sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }
    
    @RequestMapping("/rest/refresh")
    @ResponseBody
    public Map<String,String> inboxRefresh() {
        Map<String,String> result = new HashMap<String, String>();
        try {
            imapService.getMail();
        } catch (IOException e) {
            result.put("error", e.getClass().getName());
            result.put("errorMsg", e.getMessage());
        } catch (MessagingException e) {
            result.put("error", e.getClass().getName());
            result.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            result.put("error", e.getClass().getName());
            result.put("errorMsg", e.getMessage());
        }
        if (!result.containsKey("error")) {
            result.put("success", "true");
        } else {
            result.put("success", "false");
        }
        return null;
    }
}
