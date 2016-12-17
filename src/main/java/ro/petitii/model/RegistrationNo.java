package ro.petitii.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "registration_numbers")
public class RegistrationNo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String number;
    Date date;

    @OneToOne(mappedBy = "regNo")
    Petition petition;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Petition getPetition() {
        return petition;
    }

    public void setPetition(Petition petition) {
        this.petition = petition;
    }
}