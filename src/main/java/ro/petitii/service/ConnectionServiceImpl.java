package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.petitii.model.Connection;
import ro.petitii.model.Petition;
import ro.petitii.repository.ConnectionRepository;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    private ConnectionRepository connectionRepository;

    @Override
    public Connection link(Petition petition, Petition vassal) {
        Connection connection = connectionRepository.findByOldPetitionAndNewPetition(petition, vassal);
        if (connection == null) {
            connection = connectionRepository.findByOldPetitionAndNewPetition(vassal, petition);
        }

        if (connection == null) {
            connection = new Connection();
            connection.setNewPetition(petition);
            connection.setOldPetition(vassal);
            return connectionRepository.save(connection);
        } else {
            return connection;
        }
    }

    @Override
    public void unlink(Petition petition, Petition vassal) {
        Connection connection = connectionRepository.findByOldPetitionAndNewPetition(petition, vassal);
        if (connection == null) {
            connection = connectionRepository.findByOldPetitionAndNewPetition(vassal, petition);
        }

        if (connection != null) {
            connectionRepository.delete(connection);
        }
    }
}
