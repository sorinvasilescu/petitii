package ro.petitii.controller.api;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ro.petitii.model.User;
import ro.petitii.service.UserService;

import javax.validation.Valid;

import static ro.petitii.controller.api.SecurityUtils.isAdmin;

@RestController
public class UserRestController {
    @Autowired
    private UserService userService;

    @JsonView(DataTablesOutput.View.class)
    @RequestMapping(value = "/api/users", method = {RequestMethod.GET, RequestMethod.POST})
    public DataTablesOutput<User> getUsers(@Valid DataTablesInput input) {
        if (isAdmin()) {
            return userService.findAll(input);
        } else {
            return new DataTablesOutput<>();
        }
    }
}