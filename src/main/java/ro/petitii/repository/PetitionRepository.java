package ro.petitii.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.User;

@Repository
public interface PetitionRepository extends DataTablesRepository<Petition, Long> {
    Page<Petition> findByResponsible(User user, Pageable p);
    Page<Petition> findByResponsibleAndCurrentStatus(User user, PetitionStatus.Status status, Pageable p);
    Page<Petition> findByCurrentStatus(PetitionStatus.Status status, Pageable p);
    Long countByResponsible(User responsible);
}