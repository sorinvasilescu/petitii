package ro.petitii.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "petitions")
public class Petition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "registration_no")
    private RegistrationNo regNo;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date receivedDate;
    private String relation;

    @OneToOne
    @JoinColumn(name = "petitioner_id")
    private Petitioner petitioner;

    private String origin;
    private String type;
    private String field;
    @Column(name = "abstract")
    private String _abstract;
    private String description;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    private User responsible;

    @OneToMany(mappedBy = "petition")
    private Collection<Email> emails;

    public Long getId() {
        return id;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<Email> getEmails() {
        return emails;
    }

    public void setEmails(Collection<Email> emails) {
        this.emails = emails;
    }

    public RegistrationNo getRegNo() {
        return regNo;
    }

    public void setRegNo(RegistrationNo regNo) {
        this.regNo = regNo;
    }

    public Petitioner getPetitioner() {
        return petitioner;
    }

    public void setPetitioner(Petitioner petitioner) {
        this.petitioner = petitioner;
    }

    public User getResponsible() {
        return responsible;
    }

    public void setResponsible(User responsible) {
        this.responsible = responsible;
    }
}
