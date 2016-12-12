package ro.petitii.controller.rest;

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

@RestController
public class UserRestController {  
    @Autowired
    private UserService userService;

    @JsonView(DataTablesOutput.View.class)
//    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/rest/users", method = {RequestMethod.GET, RequestMethod.POST})
    public DataTablesOutput<User> getUsers(@Valid DataTablesInput input) {
        return userService.findAll(input);
    }
}