package ro.petitii.controller.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import ro.petitii.model.Contact;
import ro.petitii.service.ContactService;

@RestController
public class ContactRestController {  
    @Autowired
    private ContactService contactService;

    @JsonView(DataTablesOutput.View.class)
    @RequestMapping(value = "/api/contacts", method = RequestMethod.POST)
    public DataTablesOutput<Contact> getUsers(@Valid DataTablesInput input) {
        return contactService.findAll(input);
    }
}