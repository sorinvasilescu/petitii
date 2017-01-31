package ro.petitii.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Email;
import ro.petitii.model.Petition;


@Repository
public interface EmailRepository extends DataTablesRepository<Email,Long> {
    Page<Email> findByType(Email.EmailType type, Pageable p);
    Long countByType(Email.EmailType type);

    Page<Email> findByPetition(Petition petition, Pageable p);
    Long countByPetition(Petition petition);

    @Override
    <S extends Email> Iterable<S> save(Iterable<S> iterable);
}