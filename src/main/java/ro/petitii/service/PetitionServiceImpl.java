package ro.petitii.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.model.Petition;
import ro.petitii.model.Petitioner;
import ro.petitii.repository.PetitionRepository;
import ro.petitii.service.email.ImapService;

import java.util.Collection;

@Service
public class PetitionServiceImpl implements PetitionService {

    @Autowired
    PetitionRepository petitionRepository;

    @Autowired
    RegistrationNumberService regNoService;

    @Autowired
    PetitionerService petitionerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);

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

        return petitionRepository.save(petition);
    }

    @Override
    public Petition findById(Long id) {
        return petitionRepository.findOne(id);
    }
}
