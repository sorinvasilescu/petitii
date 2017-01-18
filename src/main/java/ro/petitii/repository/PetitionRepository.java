package ro.petitii.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Petition;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.Petitioner;
import ro.petitii.model.User;

import java.util.List;

@Repository
public interface PetitionRepository extends DataTablesRepository<Petition, Long> {
    List<Petition> findByResponsible(User user);
    Page<Petition> findByResponsible(User user, Pageable p);
    Page<Petition> findByPetitioner(Petitioner petitioner, Pageable p);
    Page<Petition> findByResponsibleAndCurrentStatusIn(User user, List<PetitionStatus.Status> statuses, Pageable p);
    Page<Petition> findByCurrentStatusIn(List<PetitionStatus.Status> statuses, Pageable p);
    Long countByResponsible(User responsible);
    Long countByPetitioner(Petitioner petitioner);
    DataTablesOutput<Petition> findAll(DataTablesInput input, Specification<Petition> spec);

    @Query("select p from Petition p " +
            "where p in (select c.oldPetition from Connection c where c.newPetition.id = :id) " +
            "or p in (select c.newPetition from Connection c where c.oldPetition.id = :id)")
    Page<Petition> findLinkedPetitions(@Param("id") Long id, Pageable p);

    @Query("select count(p) from Petition p " +
            "where p in (select c.oldPetition from Connection c where c.newPetition.id = :id) " +
            "or p in (select c.newPetition from Connection c where c.oldPetition.id = :id)")
    Long countLinkedPetitions(@Param("id") Long id);
}