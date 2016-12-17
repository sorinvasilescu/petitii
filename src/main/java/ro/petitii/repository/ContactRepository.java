package ro.petitii.repository;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import ro.petitii.model.Contact;

@Repository
public interface ContactRepository extends DataTablesRepository<Contact, Long> {
    
	List<Contact> findByEmail(String email);
	List<Contact> findByName(String name);
	List<Contact> findByPhone(String phone);
}
