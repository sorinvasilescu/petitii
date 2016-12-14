package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ListaContacteController {

    @RequestMapping("/contacte")
    public String listContacts() {
        return "lista_contacte_page";
    }
    @RequestMapping("/edit-contact")
    public String editContact() {
        return "edit_contact_page";
    }
    @RequestMapping("/adauga-contact")
    public String addContact() {
        return "add_contact_page";
    }
}