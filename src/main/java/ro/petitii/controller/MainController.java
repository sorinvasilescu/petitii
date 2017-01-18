package ro.petitii.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController implements ErrorController {
    @RequestMapping("/error")
    public String error() {
        return "error";
    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/inbox";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/editor-toolbar")
    public String editorToolbar() {
        return "fragments/editor-toolbar";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}