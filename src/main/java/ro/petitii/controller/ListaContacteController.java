package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ListaContacteController {

    @RequestMapping("/contacte")
    public String listContacts() {
        return "listacontacte_page";
    }
}