package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestPetitionResponse;

import java.util.List;

public interface PetitionService {
    Petition save(Petition petition);
    Petition findById(Long id);
    Long count();
    Long countByResponsible(User responsible);
    List<Petition> findByResponsible(User user, int startIndex, int size, Sort.Direction sortDirection, String sortcolumn);
    List<Petition> findAll(int startIndex, int size, Sort.Direction sortDirection, String sortcolumn);
    RestPetitionResponse getTableContent(User user, int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}