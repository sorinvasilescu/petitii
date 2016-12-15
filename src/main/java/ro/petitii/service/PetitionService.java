package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestPetitionResponse;

import java.util.List;

public interface PetitionService {
    Petition save(Petition petition);
    Petition findById(Long id);
    List<Petition> findByResponsible(User user, int startIndex, int size, Sort.Direction sortDirection, String sortcolumn);
    RestPetitionResponse getTableContent(User user, int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}