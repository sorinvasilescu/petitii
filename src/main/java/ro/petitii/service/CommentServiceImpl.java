package ro.petitii.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import ro.petitii.model.Comment;
import ro.petitii.model.Petition;
import ro.petitii.model.dt.DTCommentResponseElement;
import ro.petitii.repository.CommentRepository;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private CommentRepository commentRepository;

    @Inject
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public void delete(long id) {
        commentRepository.delete(id);
    }

    @Override
    public DataTablesOutput<DTCommentResponseElement> getTableContent(Petition petition, int startIndex, int size, Sort.Direction sortDirection, String sortColumn) {
        PageRequest p = new PageRequest(startIndex / size, size, sortDirection, sortColumn);
        Page<Comment> comments = commentRepository.findByPetitionId(petition.getId(), p);

        List<DTCommentResponseElement> data = new LinkedList<>();
        for (Comment e : comments.getContent()) {
            DTCommentResponseElement re = new DTCommentResponseElement();
            re.setId(e.getId());
            re.setComment(e.getComment());
            re.setPetitionId(e.getPetition().getId());
            re.setUser(e.getUser().getFullName());
            re.setDate(df.format(e.getDate()));
            data.add(re);
        }
        DataTablesOutput<DTCommentResponseElement> response = new DataTablesOutput<>();
        response.setData(data);
        Long count = commentRepository.countByPetitionId(petition.getId());
        response.setRecordsFiltered(count);
        response.setRecordsTotal(count);
        return response;
    }
}
