package ro.petitii.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.Comment;
import ro.petitii.model.Petition;
import ro.petitii.model.dt.DTCommentResponseElement;

public interface CommentService {
    Comment save(Comment comment);

    void delete(long id);

    DataTablesOutput<DTCommentResponseElement> getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection, String sortColumn);
}