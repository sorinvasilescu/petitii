package ro.petitii.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.*;

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

    @ManyToOne
    @JoinColumn(name = "petitioner_id")
    private Petitioner petitioner;

    private String origin;
    private String type;
    private String field;
    @Column(name = "abstract")
    private String subject;
    private String description;
    @Column(name = "problem_type")
    private String problemType;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    private User responsible;

    @OneToMany(mappedBy = "petition")
    private Collection<Email> emails;

    @OneToMany(mappedBy = "petition")
    private Collection<PetitionStatus> statuses;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String _abstract) {
        this.subject = _abstract;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
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

    public PetitionStatus.Status getStatus() {
        if ((statuses!=null) && (statuses.size() > 0)) {
            List<PetitionStatus> statuses = new ArrayList<>(this.statuses);
            Comparator<PetitionStatus> comparator = new Comparator<PetitionStatus>() {
                @Override
                public int compare(PetitionStatus o1, PetitionStatus o2) {
                    if (o1.getDate().before(o2.getDate())) return -1;
                    else return 1;
                }
            };
            Collections.sort(statuses, comparator);
            return statuses.get(0).getStatus();
        } else return null;
    }

    public String statusString() {
        PetitionStatus.Status status = this.getStatus();
        if (status != null) {
            return status.toString();
        } else return "NEW";
    }

    @Override
    public String toString() {
        return "Petition{" +
                "id=" + id +
                ", regNo=" + regNo +
                ", receivedDate=" + receivedDate +
                ", relation='" + relation + '\'' +
                ", petitioner=" + petitioner +
                ", origin='" + origin + '\'' +
                ", type='" + type + '\'' +
                ", field='" + field + '\'' +
                ", _abstract='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", responsible=" + responsible +
                ", emails=" + emails +
                '}';
    }
}
