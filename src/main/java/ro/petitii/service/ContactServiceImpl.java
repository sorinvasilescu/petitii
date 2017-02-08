package ro.petitii.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import ro.petitii.model.Contact;
import ro.petitii.repository.ContactRepository;

@Service
public class ContactServiceImpl implements ContactService {
	@Autowired
	private ContactRepository contactRepository;

	@Override
	public Iterable<Contact> getAllContacts() {
		return contactRepository.findAll();
	}

	@Override
	public Contact getById(Long id) {
		return contactRepository.findOne(id);
	}

	@Override
	public DataTablesOutput<Contact> findAll(DataTablesInput input) {
		return contactRepository.findAll(input);
	}

	@Override
	public Contact save(Contact contact) {
		return contactRepository.save(contact);
	}

	@Override
	public void delete(long id) {
		contactRepository.delete(id);
	}

	@Override
	public void delete(long[] contactIds) {

		List<Contact> contactList = new ArrayList<Contact>(contactIds.length);
		for (long id : contactIds) {
			contactList.add(new Contact(id));
		}

		contactRepository.delete(contactList);
	}
}
