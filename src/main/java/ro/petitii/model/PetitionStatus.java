package ro.petitii.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PetitionStatus {

    public enum Status {
        RECEIVED, IN_PROGRESS, REDIRECTED, SOLVED, CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "id_petition")
    private Petition petition;

    private Status status;
    private Date date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Petition getPetition() {
        return petition;
    }

    public void setPetition(Petition petition) {
        this.petition = petition;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}