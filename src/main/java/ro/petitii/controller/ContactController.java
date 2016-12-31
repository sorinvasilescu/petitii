package ro.petitii.controller;

import javax.validation.Valid;

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

@Controller
public class ContactController extends ControllerBase {

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
    	return editContact(contact);
    }

	@RequestMapping(path = "/contact/{id}/delete", method = RequestMethod.GET)
	public ModelAndView deleteContact(@PathVariable("id") Long id) {
		contactService.delete(id);
		return new ModelAndView("contacts_list");
	}

	private ModelAndView editContact(Contact contact) {
		ModelAndView modelAndView = new ModelAndView("contacts_crud");
    	return modelAndView.addObject("contact", contact);
	}

	@RequestMapping(path = "/contact", method = RequestMethod.POST)
    public ModelAndView saveContact(@Valid Contact contact, BindingResult bindingResult,
                                     final RedirectAttributes attr) {
		ModelAndView modelAndView;
		
		if (bindingResult.hasErrors()) {
        	modelAndView = editContact(contact);
            modelAndView.addObject("toast", createToast("Instituția nu a fost salvată", ToastType.danger));
        } else {
            Contact savedContact = contactService.save(contact);
            modelAndView = new ModelAndView("redirect:/contact/" + savedContact.getId());
            attr.addFlashAttribute("toast", createToast("Instituția a fost salvată cu succes", ToastType.success));
        }
        return modelAndView;
    }
}
