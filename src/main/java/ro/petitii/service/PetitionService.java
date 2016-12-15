package ro.petitii.service;

import ro.petitii.model.Petition;

import java.util.Collection;

public interface PetitionService {
    Petition save(Petition petition);
    Petition findById(Long id);
}
