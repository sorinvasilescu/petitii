package ro.petitii.service;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import ro.petitii.model.Contact;

public interface ContactService {
    Iterable<Contact> getAllContacts();

    DataTablesOutput<Contact> findAll(DataTablesInput input);

	Contact getById(Long id);
	Contact save(Contact contact);

    void delete(long id);

	void delete(long[] contactIds);
}
