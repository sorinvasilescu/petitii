package ro.petitii.model.dt;

public class RestCommentResponseElement {
    private Long id;
    private Long petitionId;
    private String user;
    private String comment;
    private String date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(Long petitionId) {
        this.petitionId = petitionId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
