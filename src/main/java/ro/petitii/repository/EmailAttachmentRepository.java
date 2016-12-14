package ro.petitii.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.EmailAttachment;

@Repository
public interface EmailAttachmentRepository extends PagingAndSortingRepository<EmailAttachment,Long> {
}
