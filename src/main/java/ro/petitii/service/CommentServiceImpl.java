package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import ro.petitii.model.Comment;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.datatables.CommentResponse;
import ro.petitii.repository.CommentRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static ro.petitii.util.StringUtil.prepareForView;

@Service
public class CommentServiceImpl implements CommentService {
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Comment createAndSave(User user, Petition petition, String body) {
        Comment comment = new Comment();
        comment.setComment(body);
        comment.setUser(user);
        comment.setDate(new Date());
        comment.setPetition(petition);

        return this.save(comment);
    }

    @Override
    public void delete(long id) {
        commentRepository.delete(id);
    }

    @Override
    public DataTablesOutput<CommentResponse> getTableContent(Petition petition, PageRequest p) {
        Page<Comment> comments = commentRepository.findByPetitionId(petition.getId(), p);

        List<CommentResponse> data = new LinkedList<>();
        for (Comment e : comments.getContent()) {
            CommentResponse re = new CommentResponse();
            re.setId(e.getId());
            re.setComment(prepareForView(e.getComment(), 100));
            re.setPetitionId(e.getPetition().getId());
            re.setUser(prepareForView(e.getUser().getFullName(), 30));
            re.setDate(df.format(e.getDate()));
            data.add(re);
        }
        DataTablesOutput<CommentResponse> response = new DataTablesOutput<>();
        response.setData(data);
        Long count = commentRepository.countByPetitionId(petition.getId());
        response.setRecordsFiltered(count);
        response.setRecordsTotal(count);
        return response;
    }
}
