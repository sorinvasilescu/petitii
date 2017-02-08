package ro.petitii.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Contact;

@Repository
public interface ContactRepository extends DataTablesRepository<Contact, Long> {
}
