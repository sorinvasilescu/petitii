package ro.petitii.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.User;
import ro.petitii.model.datatables.PetitionResponse;

import java.util.List;

public interface PetitionService {
    Petition save(Petition petition);

    Petition createFromEmail(Email email);

    Petition findById(Long id);

    Long count();

    Long countByResponsible(User responsible);

    List<Petition> findByResponsible(User user, PageRequest p);

    List<Petition> findByResponsibleAndStatus(User user, PetitionStatus.Status status, PageRequest p);

    List<Petition> findByStatus(PetitionStatus.Status status, PageRequest p);

    List<Petition> findAll(PageRequest p);

    DataTablesOutput<PetitionResponse> getTableContent(User user, PetitionStatus.Status status, PageRequest p);
}