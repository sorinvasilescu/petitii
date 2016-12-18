package ro.petitii.controller.rest;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ro.petitii.model.Comment;
import ro.petitii.model.Petition;
import ro.petitii.model.User;
import ro.petitii.model.rest.RestCommentResponse;
import ro.petitii.service.CommentService;
import ro.petitii.service.PetitionService;
import ro.petitii.service.UserService;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Controller
public class CommentsRestController {
    private PetitionService petitionService;
    private CommentService commentService;
    private UserService userService;

    @Inject
    public CommentsRestController(PetitionService petitionService, CommentService commentService,
                                  UserService userService) {
        this.petitionService = petitionService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @RequestMapping(value = "/rest/comments/{id}", method = RequestMethod.POST)
    @ResponseBody
    public RestCommentResponse getAllComments(@PathVariable("id") Long id, @Valid DataTablesInput input) {
        int sequenceNo = input.getDraw();

        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Integer start = input.getStart();
        Integer length = input.getLength();
        RestCommentResponse response = commentService
                .getTableContent(petition, start, length, sortDirection, sortColumn);
        response.setDraw(sequenceNo);
        return response;
    }

    @RequestMapping(value = "/rest/petition/{pid}/comment/add", method = RequestMethod.POST, consumes={"application/json"})
    @ResponseBody
    public String addComment(@PathVariable("pid") Long id, @RequestBody MiniComment miniComment) {
        Petition petition = petitionService.findById(id);
        if (petition == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<User> users = userService.findUserByEmail(auth.getName());
        if (users.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        User user = users.get(0);

        Comment comment = new Comment();
        comment.setComment(miniComment.getComment());
        comment.setUser(user);
        comment.setDate(new Date());
        comment.setPetition(petition);

        commentService.save(comment);

        return "done";
    }

    @RequestMapping(value = "/rest/petition/comment/delete/{cid}", method = RequestMethod.POST)
    @ResponseBody
    public String deleteComment(@PathVariable("cid") Long cid) {
        commentService.delete(cid);
        return "done";
    }
}
