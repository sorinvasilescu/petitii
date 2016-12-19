package ro.petitii.service;

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
	ContactRepository contactRepository;
	 
	@Override
	public Iterable<Contact> getAllContacts() {
		return contactRepository.findAll();
	}

	@Override
	public List<Contact> findUserByEmail(String email) {
		return contactRepository.findByEmail(email);
	}

	@Override
	public List<Contact> findUserByName(String name) {
		return contactRepository.findByName(name);
	}

	@Override
	public List<Contact> findUserByPhone(String phone) {
		return contactRepository.findByPhone(phone);
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

}
