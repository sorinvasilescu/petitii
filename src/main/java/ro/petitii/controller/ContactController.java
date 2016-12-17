package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ContactController extends ControllerBase{


    @RequestMapping("/contacts")
    public ModelAndView contacts() {
        ModelAndView modelAndView = new ModelAndView("contacts_page");        
            
//        modelAndView.addObject("restUrl","/rest/contacts");
        return modelAndView;
    }
    
    @RequestMapping("/edit-contact")
    public String editContact() {
        return "edit_contact_page";
    }
//    @RequestMapping("/adauga-contact")
//    public String addContact() {
//        return "add_contact_page";
//    }
}
