package ro.petitii.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import ro.petitii.util.TranslationUtil;

@Controller
public class ContactController extends ControllerBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);
	
	@Autowired
	private ContactService contactService;
	
	@Autowired
	private TranslationUtil translationService;

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
    	return editContact(contact);
    }

	private ModelAndView editContact(Contact contact) {
		ModelAndView modelAndView = new ModelAndView("contacts_crud");
    	return modelAndView.addObject("contact", contact);
	}

	@RequestMapping(path = "/contact", method = RequestMethod.POST)
    public ModelAndView saveContact(@Valid Contact contact, BindingResult bindingResult,
                                     final RedirectAttributes attr, HttpServletRequest request) {
		ModelAndView modelAndView;
		
		if (bindingResult.hasErrors()) {
			modelAndView = editContact(contact);
        	String message = translationService.i18n("controller.contact.not_saved", null, request);
            modelAndView.addObject("toast", createToast(message, ToastType.danger));
            String serializedErrors = Arrays.toString(bindingResult.getAllErrors().toArray());
			LOGGER.debug("Cannot save contact!"+ contact.toString() + "\n resons:" + serializedErrors);			
        } else {
            Contact savedContact = contactService.save(contact);
            modelAndView = new ModelAndView("redirect:/contact/" + savedContact.getId());
            String message = translationService.i18n("controller.contact.saved", null, request);
            attr.addFlashAttribute("toast", createToast(message, ToastType.success));
        }
        return modelAndView;
    }
}
