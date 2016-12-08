package ro.petitii.model;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "Emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    long uid;
    private String sender;
    private String recipients;
    private String cc;
    private String bcc;
    private Date date;
    private String subject;
    private String body;
    private float size;

    public enum EmailType {Inbox, Outbox, Spam}
    @Enumerated(EnumType.STRING)
    private EmailType type;

    @OneToMany(mappedBy = "email")
    private Collection<EmailAttachment> attachments;

    public Email() {}

    public Long getId() {
        return id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Collection<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public EmailType getType() {
        return type;
    }

    public void setType(EmailType type) {
        this.type = type;
    }
}