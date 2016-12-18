package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestPetitionResponse;

import java.util.List;

public interface PetitionService {
    Petition save(Petition petition);

    Petition createFromEmail(Email email);

    Petition findById(Long id);

    Long count();

    Long countByResponsible(User responsible);

    List<Petition> findByResponsible(User user, int startIndex, int size,
                                     Sort.Direction sortDirection, String sortColumn);

    List<Petition> findByResponsibleAndStatus(User user, PetitionStatus.Status status, int startIndex, int size,
                                              Sort.Direction sortDirection, String sortColumn);

    List<Petition> findByStatus(PetitionStatus.Status status, int startIndex, int size,
                                Sort.Direction sortDirection, String sortColumn);

    List<Petition> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortColumn);

    RestPetitionResponse getTableContent(User user, PetitionStatus.Status status,
                                         int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}