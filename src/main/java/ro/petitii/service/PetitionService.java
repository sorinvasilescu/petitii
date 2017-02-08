package ro.petitii.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.*;
import ro.petitii.model.datatables.PetitionResponse;

import java.util.List;

public interface PetitionService {
    Petition save(Petition petition);

    Petition createFromEmail(Email email);

    Petition findById(Long id);

    List<Petition> findAllByResponsible(User responsible);

    DataTablesOutput<PetitionResponse> getTableContent(DataTablesInput input, User user, List<PetitionStatus.Status> statuses);

    DataTablesOutput<PetitionResponse> getTableContent(Petition petition, Petitioner petitioner, PageRequest p);

    DataTablesOutput<PetitionResponse> getTableLinkedPetitions(Petition petition, PageRequest p);

    long countLinkedPetitions(Petition petition);
}