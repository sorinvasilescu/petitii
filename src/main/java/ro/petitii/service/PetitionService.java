package ro.petitii.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.*;
import ro.petitii.model.datatables.PetitionResponse;

import java.util.List;

public interface PetitionService {
    Petition save(Petition petition);

    Petition createFromEmail(Email email);

    Petition findById(Long id);

    DataTablesOutput<PetitionResponse> getTableContent(User user, PetitionStatus.Status status, PageRequest p);

    DataTablesOutput<PetitionResponse> getTableContent(Petition petition, Petitioner petitioner, PageRequest p);

    DataTablesOutput<PetitionResponse> getTableLinkedPetitions(Petition petition, PageRequest p);
}