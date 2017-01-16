package ro.petitii.service;

import ro.petitii.model.Petitioner;

import java.util.Collection;

public interface PetitionerService {
    Petitioner save(Petitioner petitioner);
    Collection<Petitioner> findByEmail(String email);
    Petitioner findOneByEmail(String email);
}