package ro.petitii.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestPetitionResponse;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;
import ro.petitii.service.email.ImapService;

import javax.validation.Valid;

@Controller
public class PetitionRestController {

    @Autowired
    UserService userService;

    @Autowired
    PetitionService petitionService;

    @RequestMapping(value = "/rest/petitions", method = RequestMethod.POST)
    @ResponseBody
    public RestPetitionResponse getUserPetitions(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) sortDirection = Sort.Direction.ASC;
        else if (input.getOrder().get(0).getDir().equals("desc")) sortDirection = Sort.Direction.DESC;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        RestPetitionResponse response = petitionService.getTableContent(user, input.getStart(), input.getLength(), sortDirection, sortColumn);
        response.setDraw(sequenceNo);

        return response;
    }

    @RequestMapping(value = "/rest/petitions/all")
    @ResponseBody
    public RestPetitionResponse getAllPetitions(@Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) sortDirection = Sort.Direction.ASC;
        else if (input.getOrder().get(0).getDir().equals("desc")) sortDirection = Sort.Direction.DESC;
        RestPetitionResponse response = petitionService.getTableContent(null, input.getStart(), input.getLength(), sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }
}
