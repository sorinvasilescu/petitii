package ro.petitii.model.rest;

public class RestEmailResponseElement {
    private String sender;
    private String subject;
    private String date;
    private String status;

    public RestEmailResponseElement() {}

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
