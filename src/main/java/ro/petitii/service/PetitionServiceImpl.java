package ro.petitii.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.petitii.config.DeadlineConfig;
import ro.petitii.config.DefaultsConfig;
import ro.petitii.model.*;
import ro.petitii.model.Petition_;
import ro.petitii.model.datatables.PetitionConverter;
import ro.petitii.model.datatables.PetitionResponse;
import ro.petitii.repository.PetitionRepository;
import ro.petitii.service.email.ImapService;
import ro.petitii.util.DateUtil;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PetitionServiceImpl implements PetitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImapService.class);

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
    private AttachmentService attachmentService;

    @Autowired
    private DefaultsConfig defaultsConfig;

    @Autowired
    private DeadlineConfig deadlineConfig;

    @Autowired
    private PetitionConverter converter;

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
            petition.setDeadline(DateUtil.deadline(new Date(), deadlineConfig.getDays()));

        return petition;
    }

    @Override
    public Petition createFromEmail(Email email) {
        Petition petition = new Petition();
        petition.setReceivedDate(email.getDate());
        petition.setDescription(email.getBody());
        petition.setSubject(email.getSubject());
        Petitioner petitioner = new Petitioner();
        // parse the email
        InternetAddress addr = null;
        try {
            addr = new InternetAddress(email.getSender());
        } catch (AddressException e) {
            LOGGER.error("Could not parse email address: " + email.getSender());
        }

        Petitioner existing;
        if (addr != null) {
            existing = petitionerService.findOneByEmail(addr.getAddress());
        } else {
            existing = petitionerService.findOneByEmail(email.getSender());
        }
        // if found overwrite current petitioner
        if (existing != null) {
            petitioner = existing;
        } else { // if not found, set values for the new petitioner
            petitioner.setEmail(email.getSender());
            convert(addr, petitioner);
            petitioner.setCountry(defaultsConfig.getCountry());
        }

        petition.setEmails(new LinkedList<>());
        petition.getEmails().add(email);
        petition.setPetitioner(petitioner);
        petition.setDeadline(DateUtil.deadline(email.getDate(), deadlineConfig.getDays()));
        return petition;
    }

    @Override
    public Petition findById(Long id) {
        return petitionRepository.findOne(id);
    }

    @Override
    public List<Petition> findAllByResponsible(User responsible) {
        return petitionRepository.findByResponsible(responsible);
    }

    @Override
    public DataTablesOutput<PetitionResponse> getTableContent(DataTablesInput input, User user, List<PetitionStatus.Status> statuses) {
        DataTablesOutput<PetitionResponse> petitions;
        Specification<Petition> spec = null;
        if (user != null) {
            if (statuses == null) {
                spec = (Root<Petition> root, CriteriaQuery<?> q, CriteriaBuilder cb) -> cb.equal(root.get(Petition_.responsible),user);
            } else {
                // filter by responsible and status
                spec = (Root<Petition> root, CriteriaQuery<?> q, CriteriaBuilder cb) -> {
                    Predicate responsible = cb.equal(root.get(Petition_.responsible),user);
                    Predicate status = cb.isTrue(root.get(Petition_.currentStatus).in(statuses));
                    return cb.and(responsible,status);
                };
            }
        } else {
            if (statuses != null) {
                // filter by status
                spec = (Root<Petition> root, CriteriaQuery<?> q, CriteriaBuilder cb) -> cb.isTrue(root.get(Petition_.currentStatus).in(statuses));
            }
        }

        petitions = petitionRepository.findAll(input, spec, null, converter);

        return petitions;
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

    private void convert(InternetAddress addr, Petitioner petitioner) {
        if (addr != null) {
            petitioner.setEmail(addr.getAddress());
            if (addr.getPersonal() != null && addr.getPersonal().length() > 0) {
                String[] name = addr.getPersonal().split(" ");
                // set first and last name
                if (name.length > 1) {
                    petitioner.setFirstName(name[0]);
                    petitioner.setLastName(name[1]);
                } else {
                    petitioner.setFirstName(addr.getPersonal());
                }
            }
        }
    }

    private List<PetitionResponse> convert(Page<Petition> petitions) {
        return petitions.getContent().stream().map(this::convert).collect(Collectors.toList());
    }

    private List<PetitionResponse> convertAndFilter(Page<Petition> petitions, Long filterPetitionId) {
        return petitions.getContent().stream().filter(p -> !Objects.equals(filterPetitionId, p.getId()))
                        .map(this::convert).collect(Collectors.toList());
    }

    private PetitionResponse convert(Petition petition) {
        return converter.convert(petition);
    }
}