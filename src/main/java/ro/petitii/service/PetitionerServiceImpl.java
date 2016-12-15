package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.model.Petitioner;
import ro.petitii.repository.PetitionerRepository;

import java.util.Collection;

@Service
public class PetitionerServiceImpl implements PetitionerService {

    @Autowired
    PetitionerRepository petitionerRepository;

    @Override
    public Petitioner save(Petitioner petitioner) {
        return petitionerRepository.save(petitioner);
    }

    @Override
    public Collection<Petitioner> findByEmail(String email) {
        return petitionerRepository.findByEmail(email);
    }
}