package ro.petitii.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.model.*;
import ro.petitii.model.datatables.PetitionResponse;
import ro.petitii.repository.PetitionRepository;
import ro.petitii.service.email.ImapService;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
public class PetitionServiceImpl implements PetitionService {

    @Autowired
    PetitionRepository petitionRepository;

    @Autowired
    RegistrationNumberService regNoService;

    @Autowired
    PetitionerService petitionerService;

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Autowired
    PetitionStatusService psService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    DefaultsConfig defaultsConfig;

    @Autowired
    AttachmentService attachmentService;

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
        if (petition.getRegNo() == null || petition.getRegNo().getNumber() == null) {
            petition.setRegNo(regNoService.generate());
        }

        // find petitioners from database
        Petitioner existingPetitioner;
        Collection<Petitioner> petitioners = petitionerService.findByEmail(petition.getPetitioner().getEmail());
        LOGGER.info("Petitioners size: " + petitioners.size());

        Petitioner petitioner;
        // if petitioner is not in database, insert it
        if (petitioners.isEmpty()) {
            petitioner = petition.getPetitioner();
            petitioner = petitionerService.save(petitioner);
            LOGGER.info("Petitioner saved: " + petitioner.toString());
        } else {
            existingPetitioner = petitioners.iterator().next();
            // check if the two petitioners are identical, if not, insert the new one in the db
            if (existingPetitioner.equals(petition.getPetitioner())) {
                petitioner = existingPetitioner;
            } else {
                petitioner = petitionerService.save(petition.getPetitioner());
            }
        }

        boolean createStatus = petition.getCurrentStatus() == null;

        if (createStatus) {
            petition.setCurrentStatus(PetitionStatus.Status.RECEIVED);
        }

        petition.setPetitioner(petitioner);
        petition = petitionRepository.save(petition);

        if (createStatus) {
            // if petition status does not exist, generate one
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findUserByEmail(auth.getName()).get(0);
            psService.create(PetitionStatus.Status.RECEIVED, petition, user);
        }

        // check if emails are linked
        Collection<Email> emails = petition.getEmails();
        for (Email email : emails) {
            if (email.getPetition() != petition) {
                email.setPetition(petition);
                emailService.saveAlone(email);
            }

            if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                for (Attachment att : email.getAttachments()) {
                    att.setPetition(petition);
                    attachmentService.save(att);
                }
            }
        }

        return petition;
    }

    @Override
    public Petition createFromEmail(Email email) {
        Petition petition = new Petition();
        petition.setReceivedDate(email.getDate());
        petition.setDescription(email.getBody());
        petition.setSubject(email.getSubject());
        Petitioner petitioner = new Petitioner();
        petitioner.setCountry(defaultsConfig.getCountry());
        try {
            InternetAddress addr = new InternetAddress(email.getSender());
            petitioner.setEmail(addr.getAddress());
            if (addr.getPersonal() != null && addr.getPersonal().length() > 0) {
                String[] name = addr.getPersonal().split(" ");
                if (name.length > 1) {
                    petitioner.setFirstName(name[0]);
                    petitioner.setLastName(name[1]);
                } else petitioner.setFirstName(addr.getPersonal());
            }

        } catch (AddressException e) {
            LOGGER.error("Could not parse email address: " + email.getSender());
            petitioner.setEmail(email.getSender());
        }
        petition.setEmails(new ArrayList<>());
        petition.getEmails().add(email);
        email.setPetition(petition);
        petition.setPetitioner(petitioner);
        return petition;
    }

    @Override
    public Petition findById(Long id) {
        return petitionRepository.findOne(id);
    }

    @Override
    public List<Petition> findByResponsible(User user, int startIndex, int size,
                                            Sort.Direction sortDirection, String sortColumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortColumn);
        Page<Petition> petitions = petitionRepository.findByResponsible(user, p);
        return petitions.getContent();
    }

    @Override
    public List<Petition> findByResponsibleAndStatus(User user, PetitionStatus.Status status, int startIndex, int size,
                                                     Sort.Direction sortDirection, String sortColumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortColumn);
        Page<Petition> petitions = petitionRepository.findByResponsibleAndCurrentStatus(user, status, p);
        return petitions.getContent();
    }

    @Override
    public List<Petition> findByStatus(PetitionStatus.Status status, int startIndex, int size,
                                       Sort.Direction sortDirection, String sortColumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortColumn);
        Page<Petition> petitions = petitionRepository.findByCurrentStatus(status, p);
        return petitions.getContent();
    }

    @Override
    public List<Petition> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortcolumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortcolumn);
        Page<Petition> petitions = petitionRepository.findAll(p);
        return petitions.getContent();
    }

    @Override
    public DataTablesOutput<PetitionResponse> getTableContent(User user, PetitionStatus.Status status,
                                                              int startIndex, int size,
                                                              Sort.Direction sortDirection,
                                                              String sortColumn) {
        List<Petition> petitions;
        if (user != null) {
            if (status == null) {
                petitions = this.findByResponsible(user, startIndex, size, sortDirection, sortColumn);
            } else {
                petitions = this.findByResponsibleAndStatus(user, status, startIndex, size, sortDirection, sortColumn);
            }
        } else {
            if (status == null) {
                petitions = this.findAll(startIndex, size, sortDirection, sortColumn);
            } else {
                petitions = this.findByStatus(status, startIndex, size, sortDirection, sortColumn);
            }
        }
        DataTablesOutput<PetitionResponse> response = new DataTablesOutput<>();
        List<PetitionResponse> data = new ArrayList<>();
        for (Petition petition : petitions) {
            PetitionResponse element = new PetitionResponse();
            element.setId(petition.getId());
            element.set_abstract(petition.getSubject());
            element.setPetitionerEmail(petition.getPetitioner().getEmail());
            element.setPetitionerName(petition.getPetitioner().getFirstName() + " " +
                                              petition.getPetitioner().getLastName());
            element.setRegNo(petition.getRegNo().getNumber());
            element.setStatus(messageSource.getMessage(petition.statusString(), null, new Locale("ro")));
            data.add(element);
        }
        response.setData(data);
        Long count;
        if (user != null) count = this.countByResponsible(user);
        else count = this.count();
        response.setRecordsTotal(count);
        response.setRecordsFiltered(count);

        return response;
    }
}