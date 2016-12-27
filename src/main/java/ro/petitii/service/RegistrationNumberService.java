package ro.petitii.service;

import ro.petitii.model.RegistrationNo;

public interface RegistrationNumberService {
    RegistrationNo generate();

    RegistrationNo findById(long id);
}
