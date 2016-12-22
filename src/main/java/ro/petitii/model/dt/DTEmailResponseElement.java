package ro.petitii.model.dt;

public class DTEmailResponseElement {
    private Long id;
    private String sender;
    private String subject;
    private String date;
    private Long petition_id;

    public DTEmailResponseElement() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String dubject) {
        this.subject = dubject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getPetition_id() {
        return petition_id;
    }

    public void setPetition_id(Long petition_id) {
        this.petition_id = petition_id;
    }
}
