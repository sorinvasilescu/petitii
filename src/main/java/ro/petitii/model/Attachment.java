package ro.petitii.model;

import javax.mail.BodyPart;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "original_filename")
    private String originalFilename;

    private String filename;

    @Column(name = "content_type")
    private String contentType;

    private Date date;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "emails_attachments",
            joinColumns = { @JoinColumn(name = "attachment_id") },
            inverseJoinColumns = { @JoinColumn(name = "email_id") },
            uniqueConstraints = { @UniqueConstraint(columnNames = {"attachment_id","email_id"}) })
    private List<Email> emails;

    @ManyToOne
    @JoinColumn(name = "petition_id")
    private Petition petition;

    @Transient
    private BodyPart bodyPart;

    public Attachment() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> email) {
        this.emails = email;
    }

    public Petition getPetition() {
        return petition;
    }

    public void setPetition(Petition petition) {
        this.petition = petition;
    }

    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }
}
