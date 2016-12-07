package ro.petitii.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Email;

import java.util.List;

@Repository
public interface EmailRepository extends PagingAndSortingRepository<Email,Long> {
    Email save(Email e);
}
