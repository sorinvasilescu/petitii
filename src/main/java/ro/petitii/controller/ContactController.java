package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.petitii.model.Contact;
import ro.petitii.service.ContactService;

import javax.validation.Valid;

import static ro.petitii.validation.ValidationUtil.*;

@Controller
public class ContactController extends ViewController {
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @RequestMapping("/contacts")
    public ModelAndView contacts() {
        return new ModelAndView("contacts_list");
    }

    @RequestMapping(path = "/contact", method = RequestMethod.GET)
    public ModelAndView newContact() {
        Contact newContact = new Contact();
        return editContact(newContact);
    }

    @RequestMapping(path = "/contact/{id}", method = RequestMethod.GET)
    public ModelAndView editContact(@PathVariable("id") Long id) {
        Contact contact = contactService.getById(id);
        check(assertNotNull(contact, i18n("controller.contact.invalid_id")), logger, redirect("contacts"));
        return editContact(contact);
    }

    private ModelAndView editContact(Contact contact) {
        ModelAndView modelAndView = new ModelAndView("contacts_crud");
        return modelAndView.addObject("contact", contact);
    }

    @RequestMapping(path = "/contact", method = RequestMethod.POST)
    public ModelAndView saveContact(@Valid Contact contact, BindingResult bindResult, final RedirectAttributes attr) {
        check(bindResult, i18n("controller.contact.not_saved"), logger, editContact(contact));
        Contact savedContact = contactService.save(contact);
        return successAndRedirect(i18n("controller.contact.saved"), "contact/" + savedContact.getId(), attr);
    }
}
