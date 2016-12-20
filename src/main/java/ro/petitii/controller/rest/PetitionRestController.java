package ro.petitii.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestPetitionResponse;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;

import javax.validation.Valid;

@Controller
public class PetitionRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private PetitionService petitionService;

    @RequestMapping(value = "/rest/petitions/user", method = RequestMethod.POST)
    @ResponseBody
    public RestPetitionResponse getUserPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);

        PetitionStatus.Status pStatus = parseStatus(status);

        Integer start = input.getStart();
        Integer length = input.getLength();
        RestPetitionResponse response =
                petitionService.getTableContent(user, pStatus, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);

        return response;
    }

    @RequestMapping(value = "/rest/petitions/all", method = RequestMethod.POST)
    @ResponseBody
    public RestPetitionResponse getAllPetitions(@Valid DataTablesInput input, String status) {
        int sequenceNo = input.getDraw();

        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        PetitionStatus.Status pStatus = parseStatus(status);
        Integer start = input.getStart();
        Integer length = input.getLength();
        RestPetitionResponse response =
                petitionService.getTableContent(null, pStatus, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    private PetitionStatus.Status parseStatus(String status) {
        if ("started".equalsIgnoreCase(status)) {
            return PetitionStatus.Status.IN_PROGRESS;
        } else {
            return null;
        }
    }
}
