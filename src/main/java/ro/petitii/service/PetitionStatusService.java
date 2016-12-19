package ro.petitii.service;

import ro.petitii.model.Petition;
import ro.petitii.model.PetitionStatus;
import ro.petitii.model.User;

public interface PetitionStatusService {
    PetitionStatus create(PetitionStatus.Status status, Petition petition, User user);
}
