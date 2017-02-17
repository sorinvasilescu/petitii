package ro.petitii.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Email;


@Repository
public interface EmailRepository extends DataTablesRepository<Email, Long> {
    @Override
    <S extends Email> Iterable<S> save(Iterable<S> iterable);
}