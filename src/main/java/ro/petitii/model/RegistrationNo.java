package ro.petitii.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "registration_numbers")
public class RegistrationNo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String number;
    private Date date;

    @OneToOne(mappedBy = "regNo")
    private Petition petition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @Override
    public String toString() {
        return this.number;
    }
}