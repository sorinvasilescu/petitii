package ro.petitii.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import ro.petitii.model.Comment;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.datatables.CommentResponse;

public interface CommentService {
    Comment save(Comment comment);

    Comment createAndSave(User user, Petition petition, String body);

    void delete(long id);

    DataTablesOutput<CommentResponse> getTableContent(Petition petition, PageRequest p);
}