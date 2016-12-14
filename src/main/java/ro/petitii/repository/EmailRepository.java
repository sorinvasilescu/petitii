package ro.petitii.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Email;


@Repository
public interface EmailRepository extends PagingAndSortingRepository<Email,Long> {
    Page<Email> findByType(Email.EmailType type, Pageable p);
    Long countByType(Email.EmailType type);
}