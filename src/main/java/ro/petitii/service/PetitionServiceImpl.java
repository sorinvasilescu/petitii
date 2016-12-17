package ro.petitii.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.Petitioner;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestPetitionResponse;
import ro.petitii.model.rest.RestPetitionResponseElement;
import ro.petitii.repository.PetitionRepository;
import ro.petitii.service.email.ImapService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class PetitionServiceImpl implements PetitionService {

    @Autowired
    PetitionRepository petitionRepository;

    @Autowired
    RegistrationNumberService regNoService;

    @Autowired
    PetitionerService petitionerService;

    @Autowired
    UserService userService;

    @Autowired
    PetitionStatusService psService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);

    @Override
    public Long count() {
        return petitionRepository.count();
    }

    @Override
    public Long countByResponsible(User responsible) {
        return petitionRepository.countByResponsible(responsible);
    }

    @Override
    public Petition save(Petition petition) {

        // if registration number does not exist, generate one
        if (petition.getRegNo() == null) {
            petition.setRegNo(regNoService.generate());
        }

        // find petitioners from database
        Collection<Petitioner> petitioners = petitionerService.findByEmail(petition.getPetitioner().getEmail());
        LOGGER.info("Petitioners size: " + petitioners.size());

        Petitioner petitioner;
        // if petitioner is not in database, insert it
        if (petitioners.isEmpty()) {
            petitioner = petition.getPetitioner();
            petitioner = petitionerService.save(petitioner);
            LOGGER.info("Petitioner saved: " + petitioner.toString());
        } else {
            petitioner = petitioners.iterator().next();
        }
        petition.setPetitioner(petitioner);
        petition = petitionRepository.save(petition);

        // if petition status does not exist, generate one
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName()).get(0);
        psService.create(PetitionStatus.Status.RECEIVED,petition, user);

        return petition;
    }

    @Override
    public Petition findById(Long id) {
        return petitionRepository.findOne(id);
    }

    @Override
    public List<Petition> findByResponsible(User user, int startIndex, int size, Sort.Direction sortDirection, String sortcolumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortcolumn);
        Page<Petition> petitions = petitionRepository.findByResponsible(user,p);
        return petitions.getContent();
    }

    @Override
    public List<Petition> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortcolumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortcolumn);
        Page<Petition> petitions = petitionRepository.findAll(p);
        return petitions.getContent();
    }

    @Override
    public RestPetitionResponse getTableContent(User user, int startIndex, int size, Sort.Direction sortDirection, String sortColumn) {
        List<Petition> petitions;
        if (user != null) petitions = this.findByResponsible(user, startIndex, size, sortDirection, sortColumn);
        else petitions = this.findAll(startIndex,size,sortDirection,sortColumn);
        RestPetitionResponse response = new RestPetitionResponse();
        List<RestPetitionResponseElement> data = new ArrayList<>();
        for (Petition petition : petitions) {
            RestPetitionResponseElement element = new RestPetitionResponseElement();
            element.setId(petition.getId());
            element.set_abstract(petition.get_abstract());
            element.setPetitionerEmail(petition.getPetitioner().getEmail());
            element.setPetitionerName(petition.getPetitioner().getFirstName() + " " + petition.getPetitioner().getLastName());
            element.setRegNo(petition.getRegNo().getNumber());
            // TODO status
            element.setStatus("TODO: Status");
            data.add(element);
        }
        response.setData(data);
        Long count;
        if (user!=null) count = this.countByResponsible(user);
        else count = this.count();
        response.setRecordsTotal(count);
        response.setRecordsFiltered(count);

        return response;
    }
}