package ro.petitii.service;

import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import ro.petitii.model.Contact;

public interface ContactService {
    Iterable<Contact> getAllContacts();

    List<Contact> findUserByEmail(String email);
    List<Contact> findUserByName(String name);
    List<Contact> findUserByPhone(String phone);

    DataTablesOutput<Contact> findAll(DataTablesInput input);

	Contact getById(Long id);
	Contact save(Contact contact);
}
