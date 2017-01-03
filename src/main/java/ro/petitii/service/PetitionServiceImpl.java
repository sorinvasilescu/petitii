package ro.petitii.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.model.*;
import ro.petitii.model.datatables.PetitionResponse;
import ro.petitii.repository.PetitionRepository;
import ro.petitii.service.email.ImapService;
import ro.petitii.util.DateUtil;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static ro.petitii.util.StringUtil.prepareForView;

@Service
public class PetitionServiceImpl implements PetitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private PetitionRepository petitionRepository;

    @Autowired
    private RegistrationNumberService regNoService;

    @Autowired
    private PetitionerService petitionerService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private PetitionStatusService psService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DefaultsConfig defaultsConfig;

    @Autowired
    private AttachmentService attachmentService;

    @Override
    public Petition save(Petition petition) {
        // if registration number does not exist, generate one
        if (petition.getRegNo() == null || petition.getRegNo().getId() == null) {
            petition.setRegNo(regNoService.generate());
        } else {
            petition.setRegNo(regNoService.findById(petition.getRegNo().getId()));
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
                emailService.save(email);
            }

            if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                for (Attachment att : email.getAttachments()) {
                    att.setPetition(petition);
                    attachmentService.save(att);
                }
            }
        }

        // check if deadline is not set
        if (petition.getDeadline() == null)
            petition.setDeadline(DateUtil.deadline(new Date()));

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
        petition.setPetitioner(petitioner);
        petition.setDeadline(DateUtil.deadline(email.getDate()));
        return petition;
    }

    @Override
    public Petition findById(Long id) {
        return petitionRepository.findOne(id);
    }

    @Override
    public DataTablesOutput<PetitionResponse> getTableContent(User user, PetitionStatus.Status status,
                                                              PageRequest p) {
        Page<Petition> petitions;
        if (user != null) {
            if (status == null) {
                petitions = petitionRepository.findByResponsible(user, p);
            } else {
                petitions = petitionRepository.findByResponsibleAndCurrentStatus(user, status, p);
            }
        } else {
            if (status == null) {
                petitions = petitionRepository.findAll(p);
            } else {
                petitions = petitionRepository.findByCurrentStatus(status, p);
            }
        }
        DataTablesOutput<PetitionResponse> response = new DataTablesOutput<>();
        response.setData(convert(petitions));
        Long count;
        if (user != null) {
            count = petitionRepository.countByResponsible(user);
        } else {
            count = petitionRepository.count();
        }
        response.setRecordsTotal(count);
        response.setRecordsFiltered(count);

        return response;
    }

    @Override
    public DataTablesOutput<PetitionResponse> getTableContent(Petition petition, Petitioner petitioner, PageRequest p) {
        Page<Petition> petitions = petitionRepository.findByPetitioner(petitioner, p);
        DataTablesOutput<PetitionResponse> response = new DataTablesOutput<>();
        response.setData(convertAndFilter(petitions, petition.getId()));
        Long count = petitionRepository.countByPetitioner(petitioner) - 1;
        response.setRecordsTotal(count);
        response.setRecordsFiltered(count);
        return response;
    }

    @Override
    public DataTablesOutput<PetitionResponse> getTableLinkedPetitions(Petition petition, PageRequest p) {
        Page<Petition> petitions = petitionRepository.findLinkedPetitions(petition.getId(), p);
        DataTablesOutput<PetitionResponse> response = new DataTablesOutput<>();
        response.setData(convert(petitions));
        Long count = petitionRepository.countLinkedPetitions(petition.getId());
        response.setRecordsTotal(count);
        response.setRecordsFiltered(count);
        return response;
    }

    @Override
    public long countLinkedPetitions(Petition petition) {
        return petitionRepository.countLinkedPetitions(petition.getId());
    }

    private List<PetitionResponse> convert(Page<Petition> petitions) {
        return petitions.getContent().stream().map(this::convert).collect(Collectors.toList());
    }

    private List<PetitionResponse> convertAndFilter(Page<Petition> petitions, Long filterPetitionId) {
        return petitions.getContent().stream().filter(p -> !Objects.equals(filterPetitionId, p.getId()))
                        .map(this::convert).collect(Collectors.toList());
    }

    private PetitionResponse convert(Petition petition) {
        PetitionResponse element = new PetitionResponse();
        element.setId(petition.getId());
        element.set_abstract(prepareForView(petition.getSubject(), 100));
        element.setPetitionerEmail(petition.getPetitioner().getEmail());
        element.setPetitionerName(prepareForView(petition.getPetitioner().getFullName(), 30));
        element.setUser(petition.getResponsible().getFullName());
        element.setReceivedDate(df.format(petition.getReceivedDate()));
        element.setLastUpdateDate(df.format(petition.getLastUpdateDate()));
        element.setRegNo(petition.getRegNo().getNumber());
        element.setStatus(messageSource.getMessage(petition.statusString(), null, new Locale("ro")));
        return element;
    }
}