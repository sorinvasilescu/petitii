package ro.petitii.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    @Valid
    private Petitioner petitioner;

    private String origin;
    private String type;
    private String field;

    @Column(name = "abstract")
    @Size(min = 5)
    private String subject;

    @NotNull
    private String description;

    @Column(name = "problem_type")
    private String problemType;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    @NotNull
    private User responsible;

    @OneToMany(mappedBy = "petition")
    private Collection<Email> emails;

    @OneToMany(mappedBy = "petition")
    private Collection<PetitionStatus> statuses;

    @Column(name = "status")
    private PetitionStatus.Status currentStatus;

    @OneToMany(mappedBy = "petition")
    private Collection<Attachment> attachments;

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

    public PetitionStatus.Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(PetitionStatus.Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Collection<PetitionStatus> getStatuses() {
        return statuses;
    }

    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String statusString() {
        PetitionStatus.Status status = this.getCurrentStatus();
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
