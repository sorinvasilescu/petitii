package ro.petitii.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ro.petitii.model.User;
import ro.petitii.service.UserService;

import javax.validation.Valid;
import java.util.UUID;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController extends ControllerBase {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @RequestMapping("/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users_list");
        modelAndView.addObject("page", "inbox");
        modelAndView.addObject("title", "Useri");
        modelAndView.addObject("apiUrl", "/api/users");
        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editUser(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("users_crud");

        User user = userService.findById(id);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @RequestMapping(path = "/user/{id}/suspend", method = RequestMethod.GET)
    public ModelAndView removeUser(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(User.UserRole.SUSPENDED);
        userService.save(user);

        return new ModelAndView("redirect:/users");
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public ModelAndView addUser() {
        User user = new User();
        user.setRole(User.UserRole.USER);

        ModelAndView modelAndView = new ModelAndView("users_crud");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    private void setNewPassword(User user) {
        if (user.getChangePassword()) {
            if (!StringUtils.isEmpty(user.getPassword())
                    && !StringUtils.isEmpty(user.getPasswordCopy()) &&
                    user.getPassword().equals(user.getPasswordCopy())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                throw new RuntimeException("Password not provided");
            }
        } else {
            if (user.getId() == null) {
                //set some random temporary password
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            } else {
                //set the previous password
                user.setPassword(userService.findById(user.getId()).getPassword());
            }
        }
    }

    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public ModelAndView saveUser(@Valid User user) {

        setNewPassword(user);

        LOGGER.info(user.toString());
        userService.save(user);

        return new ModelAndView("redirect:/users");
    }

}
