package ro.petitii.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController extends ControllerBase{


    @RequestMapping("/users")
    public ModelAndView inbox() {
        ModelAndView modelAndView = new ModelAndView("users_page");
        modelAndView.addObject("page","inbox");
        modelAndView.addObject("title","Useri");
        modelAndView.addObject("restUrl","/rest/users");
        return modelAndView;
    }
    
}
