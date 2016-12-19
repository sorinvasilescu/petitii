package ro.petitii.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import ro.petitii.model.Comment;

public interface CommentRepository extends DataTablesRepository<Comment, Long> {
    long countByPetitionId(long petitionId);

    Page<Comment> findByPetitionId(long petitionId, Pageable p);
}
