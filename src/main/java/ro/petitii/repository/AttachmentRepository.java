package ro.petitii.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ro.petitii.model.Attachment;

@Repository
public interface AttachmentRepository extends PagingAndSortingRepository<Attachment, Long> {
    Page<Attachment> findByPetitionId(long petitionId, Pageable p);

    long countByPetitionId(long petitionId);
}
