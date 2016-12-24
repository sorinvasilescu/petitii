package ro.petitii.service;

import ro.petitii.model.Connection;
import ro.petitii.model.Petition;

public interface ConnectionService {
    Connection link(Petition petition, Petition vassal);

    void unlink(Petition petition, Petition vassal);
}
