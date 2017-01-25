package ro.petitii.controller.api;

import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.petitii.controller.BaseController;
import ro.petitii.model.Contact;
import ro.petitii.service.ContactService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ContactApiController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ContactApiController.class);

    @Autowired
    private ContactService contactService;

    @JsonView(DataTablesOutput.View.class)
    @RequestMapping(value = "/api/contacts", method = RequestMethod.POST)
    public DataTablesOutput<Contact> getContacts(@Valid DataTablesInput input) {
        return contactService.findAll(input);
    }

    @RequestMapping(path = "/api/contacts/delete", method = RequestMethod.POST)
    public Map<String, String> deleteContact(@RequestParam("contacts[]") long[] contactIds) {
        Map<String, String> result = new HashMap<>();
        try {
            contactService.delete(contactIds);
            result.put("success", "true");
            result.put("errorMsg", i18n("api.controller.contact.delete_successful"));
        } catch (Exception e) {
            logger.info("Cannot delete contacts: " + Arrays.toString(contactIds), e);
            result.put("success", "false");
            result.put("errorMsg", i18n("api.controller.contact.delete_failed"));
        }
        return result;
    }
}