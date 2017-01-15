package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.model.Petitioner;
import ro.petitii.repository.PetitionerRepository;

import java.util.Collection;
import java.util.List;

@Service
public class PetitionerServiceImpl implements PetitionerService {
    @Autowired
    private PetitionerRepository petitionerRepository;

    @Override
    public Petitioner save(Petitioner petitioner) {
        return petitionerRepository.save(petitioner);
    }

    @Override
    public Collection<Petitioner> findByEmail(String email) {
        return petitionerRepository.findByEmail(email);
    }

    @Override
    public Petitioner findOneByEmail(String email) {
        List<Petitioner> petitioners = petitionerRepository.findByEmail(email);
        if (petitioners == null || petitioners.isEmpty()) {
            return null;
        } else {
            return petitioners.get(petitioners.size() - 1);
        }
    }
}