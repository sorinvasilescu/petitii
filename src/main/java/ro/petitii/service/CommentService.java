package ro.petitii.service;

import org.springframework.data.domain.Sort;
import ro.petitii.model.Comment;
import ro.petitii.model.Petition;
import ro.petitii.model.rest.RestCommentResponse;

public interface CommentService {
    Comment save(Comment comment);

    void delete(long id);

    RestCommentResponse getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}