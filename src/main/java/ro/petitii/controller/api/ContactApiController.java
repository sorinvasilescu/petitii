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
import ro.petitii.model.Contact;
import ro.petitii.service.ContactService;

import javax.validation.Valid;
import java.util.Arrays;

@RestController
public class ContactApiController extends ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ContactApiController.class);

    @Autowired
    private ContactService contactService;

    @JsonView(DataTablesOutput.View.class)
    @RequestMapping(value = "/api/contacts", method = RequestMethod.POST)
    public DataTablesOutput<Contact> getContacts(@Valid DataTablesInput input) {
        return contactService.findAll(input);
    }

    @RequestMapping(path = "/api/contacts/delete", method = RequestMethod.POST)
    public ApiResult deleteContact(@RequestParam("contacts[]") long[] contactIds) {
        try {
            contactService.delete(contactIds);
            return success("api.controller.contact.delete_successful");
        } catch (Exception e) {
            logger.info("Cannot delete contacts: " + Arrays.toString(contactIds), e);
            return fail("api.controller.contact.delete_failed");
        }
    }
}